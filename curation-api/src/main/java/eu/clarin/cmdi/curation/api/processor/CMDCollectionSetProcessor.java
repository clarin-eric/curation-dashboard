package eu.clarin.cmdi.curation.api.processor;

import eu.clarin.cmdi.curation.api.cache.CollectionReportCache;
import eu.clarin.cmdi.curation.api.cache.DirChecksumCache;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDCollectionSet;

import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;

import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;

import eu.clarin.cmdi.curation.api.subprocessor.collection.CollectionLinkchecker;
import eu.clarin.cmdi.curation.api.subprocessor.collection.CollectionUpdater;
import eu.clarin.linkchecker.persistence.repository.UrlContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Component
@Scope(value = "prototype")
@Slf4j
@RequiredArgsConstructor
public class CMDCollectionSetProcessor {

    private final ApiConfig apiConfig;
    private final ApplicationContext applicationContext;
    private final DirChecksumCache dirChecksumCache;
    private final CollectionReportCache collectionReportCache;

    private final UrlContextRepository urlContextRepository;


    public Collection<CollectionReport> process(CMDCollectionSet cmdCollectionSet) {

        final Map<String, CollectionReport> collectionReports = new HashMap<>();

        cmdCollectionSet.getPaths().forEach(path -> {
            try {
                processCollection(path, collectionReports);
            }
            catch (MalFunctioningProcessorException e) {
                throw new RuntimeException(e);
            }
        });

        final CollectionLinkchecker collectionLinkchecker = applicationContext.getBean(CollectionLinkchecker.class);
        final CollectionUpdater  collectionUpdater = applicationContext.getBean(CollectionUpdater.class);

        collectionReports.values().forEach(collectionReport -> {

            // adding linkchecker statistics
            collectionLinkchecker.process(collectionReport);
            // calculate sums and averages over all section-reports for collection report
            collectionUpdater.process(collectionReport);
        });

        return collectionReports.values();
    }

    private void processCollection(Path path, Map<String, CollectionReport> collectionReports) throws MalFunctioningProcessorException {

        if (isCollectionRoot(path)) { // root directory of a collection

            CollectionReport collectionReport;
            CMDCollection cmdCollection = applicationContext.getBean(CMDCollection.class, path);

            // checksum approach activated, checksum hasn't changed and a previous collection report is available
            if (apiConfig.isUseChecksum() && this.dirChecksumCache.getChecksum(path) == this.dirChecksumCache.getNewChecksum(path) && Objects.nonNull(this.collectionReportCache.getCollectionReport(cmdCollection))) {

                collectionReport = this.collectionReportCache.getCollectionReport(cmdCollection);
                // setting ingestionDate to now, since we don't process the links
                urlContextRepository.updateIngestionDate(collectionReport.getName());
            }
            else { //generate new report and cache it

                collectionReport = this.collectionReportCache.getNewCollectionReport(cmdCollection);
            }
        }
        else { // parent directory of collection directories
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                directoryStream.forEach(dir -> {
                    try {
                        processCollection(dir, collectionReports);
                    }
                    catch (MalFunctioningProcessorException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            catch (IOException e) {
                log.error("can't create directory stream for path '{}'", path);
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isCollectionRoot(Path path) {

        try (Stream<Path> stream = Files.walk(path, 1)) {
            return stream.anyMatch(Files::isRegularFile);
        }
        catch (IOException e) {

            log.error("can't walk through path '{}'", path);
            throw new RuntimeException(e);
        }
    }
}
