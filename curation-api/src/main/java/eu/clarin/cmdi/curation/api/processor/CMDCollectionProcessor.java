package eu.clarin.cmdi.curation.api.processor;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.subprocessor.collection.CollectionAggregator;
import eu.clarin.cmdi.curation.api.subprocessor.collection.CollectionUpdater;
import eu.clarin.cmdi.curation.api.utils.DirChecksum;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@Scope("prototype")
public class CMDCollectionProcessor {

    private final ApiConfig conf;
    private final CollectionAggregator collectionAggregator;
    private final CollectionUpdater collectionUpdater;
    private final DirChecksum dirChecksum;

    public CMDCollectionProcessor(ApiConfig conf, CollectionAggregator collectionAggregator, CollectionUpdater collectionUpdater, DirChecksum dirChecksum) {
        this.conf = conf;
        this.collectionAggregator = collectionAggregator;
        this.collectionUpdater = collectionUpdater;
        this.dirChecksum = dirChecksum;
    }

    public CollectionReport process(CMDCollection collection) {


        LocalDateTime start = LocalDateTime.now();

        CollectionReport report = null;

        log.info("Started report generation for collection: " + collection.getPath());

        if (collection.getPath().getNameCount() >= 1) {

            String providerName = collection.getPath().getName(collection.getPath().getNameCount() - 1).toString();

            if(!this.dirChecksum.hasChanged(collection.getPath())){ // no changes for collection
                Path reportPath = conf.getDirectory().getOut().resolve("xml").resolve("collection").resolve(providerName + ".xml");

                if(Files.exists(reportPath)){
                    log.info("no changes in collection - updating previous report '{}'",  reportPath);
                    try {
                        JAXBContext ctx = JAXBContext.newInstance(CollectionReport.class);
                        Unmarshaller unmarshaller = ctx.createUnmarshaller();
                        report = (CollectionReport) unmarshaller.unmarshal(reportPath.toFile());
                        collectionUpdater.process(report);

                    }
                    catch (JAXBException e) {
                        log.error("cannot unmarshal collection report '{}'", reportPath, e);
                    }
                }
            }
            if(report == null) {
                report = new CollectionReport();

                report.fileReport.provider = providerName;
                collectionAggregator.process(collection, report);
            }

            LocalDateTime end = LocalDateTime.now();

            log.info("It took {} to generate the report for collection: {}", Duration.between(start, end).toString(), report.getName());
        }
        else {
            log.error("the provider group is the last name in the path. Therefore the collection path MUSTN'T be the root directory");
        }

        return report;
    }
}
