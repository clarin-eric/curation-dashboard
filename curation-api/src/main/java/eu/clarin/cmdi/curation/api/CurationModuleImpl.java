package eu.clarin.cmdi.curation.api;

import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDCollectionSet;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport.CategoryReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport.Context;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport.StatusDetailReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.linkchecker.persistence.model.AggregatedStatus;
import eu.clarin.linkchecker.persistence.model.StatusDetail;
import eu.clarin.linkchecker.persistence.repository.StatusRepository;
import eu.clarin.linkchecker.persistence.utils.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


/**
 * The type Curation module.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CurationModuleImpl implements CurationModule {

    private final ApplicationContext ctx;

    private final StatusRepository sRep;

    /**
     * Process cmd profile cmd profile report.
     *
     * @param schemaLocation the schema location
     * @return the cmd profile report
     */
    @Override
    public CMDProfileReport processCMDProfile(String schemaLocation) {
        try {
            CMDProfile cmdProfile = ctx.getBean(CMDProfile.class);
            cmdProfile.setSchemaLocation(schemaLocation);
            cmdProfile.setCacheable(true);

            return cmdProfile.generateReport();
        }
        catch (MalFunctioningProcessorException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Process cmd profile cmd profile report.
     *
     * @param path the path
     * @return the cmd profile report
     */
    @Override
    public CMDProfileReport processCMDProfile(Path path) {

        return processCMDProfile(path.toUri().toString());
    }

    /**
     * Process cmd instance
     *
     * @param path the path
     * @return the cmd instance report
     */
    @Override
    public CMDInstanceReport processCMDInstance(Path path) {
        if (Files.exists(path)) {
            try {
                CMDInstance cmdInstance = ctx.getBean(CMDInstance.class);
                cmdInstance.setPath(path);
                cmdInstance.setSize(Files.size(path));

                return cmdInstance.generateReport();
            }
            catch (IOException | BeansException | MalFunctioningProcessorException e) {

                throw new RuntimeException(e);

            }
        }

        else {

            log.error("path '{}' does not exist", path);
            throw new RuntimeException();

        }
    }

    /**
     * Process cmd instance cmd instance report.
     *
     * @param url the url
     * @return the cmd instance report
     */
    @Override
    public CMDInstanceReport processCMDInstance(URL url) {

        try {

            Path cmdiFilePath = Files.createTempFile(FileNameEncoder.encode(url.toString()), "xml");

            FileUtils.copyURLToFile(url, cmdiFilePath.toFile());

            long size = Files.size(cmdiFilePath);

            CMDInstance cmdInstance = ctx.getBean(CMDInstance.class);
            cmdInstance.setPath(cmdiFilePath);
            cmdInstance.setSize(size);
            cmdInstance.setUrl(url.toString());

            CMDInstanceReport report = cmdInstance.generateReport();

            // Files.delete(path);

            report.fileReport.location = url.toString();

            return report;
        }
        catch (IOException | MalFunctioningProcessorException e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Process collection
     *
     * @param path the path
     * @return the collection report
     */
    @Override
    public CollectionReport processCollection(Path path) throws MalFunctioningProcessorException {

        CMDCollection cmdCollection = ctx.getBean(CMDCollection.class);
        cmdCollection.setPath(path);

        return cmdCollection.generateReport();

    }

    @Override
    public Collection<CollectionReport> processCollectionSet(Collection<Path> paths) throws MalFunctioningProcessorException {

        CMDCollectionSet cmdCollectionSet = ctx.getBean(CMDCollectionSet.class);
        cmdCollectionSet.setPaths(paths);

        return cmdCollectionSet.generateReport();
    }

    /**
     * Gets linkchecker detail reports.
     *
     * @return the linkchecker detail reports
     */
    @Override
    @Transactional
    public Collection<LinkcheckerDetailReport> getLinkcheckerDetailReports() {

        final Collection<LinkcheckerDetailReport> linkcheckerDetailReports = new ArrayList<LinkcheckerDetailReport>();

        final LinkcheckerDetailReport[] lastLinkcheckerDetailReport = new LinkcheckerDetailReport[1];
        final CategoryReport[] lastCategoryReport = new CategoryReport[1];
        final StatusDetailReport[] lastStatusDetailReport = new StatusDetailReport[1];


        try (Stream<AggregatedStatus> aStream = sRep.findAggregatedStatus()) {
            // we iterate through a list grouped by providergroup-name and category
            aStream.forEach(aStatus -> {

                if(lastStatusDetailReport[0] == null || !lastLinkcheckerDetailReport[0].getName().equals(aStatus.getProvidergroupName())) {

                    lastLinkcheckerDetailReport[0] = new LinkcheckerDetailReport(aStatus.getProvidergroupName());
                    linkcheckerDetailReports.add(lastLinkcheckerDetailReport[0]);
                }

                if(lastCategoryReport[0] == null || !lastCategoryReport[0].getCategory().equals(aStatus.getCategory())) {

                    lastCategoryReport[0] = new CategoryReport(aStatus.getCategory());
                    lastLinkcheckerDetailReport[0].getCategoryReports().add(lastCategoryReport[0]);
                }
                // for each providergroup and category we get the 31 latest status results
                try (Stream<StatusDetail> sdStream = sRep.findStatusDetail(aStatus.getProvidergroupName(), aStatus.getCategory())) {

                    final AtomicInteger counter = new AtomicInteger();

                    sdStream.takeWhile(statusDetail -> counter.get() < 32).forEach(statusDetail -> {

                        if (lastStatusDetailReport[0] == null || !lastStatusDetailReport[0].getUrl().equals(statusDetail.getUrlname())) {

                            counter.incrementAndGet();

                            lastStatusDetailReport[0] = new StatusDetailReport(
                                    statusDetail.getUrlname(),
                                    statusDetail.getMethod(),
                                    statusDetail.getStatusCode(),
                                    statusDetail.getMessage(),
                                    statusDetail.getCheckingDate(),
                                    statusDetail.getContentType(),
                                    statusDetail.getContentLength(),
                                    statusDetail.getDuration(),
                                    statusDetail.getRedirectCount()
                            );

                            lastCategoryReport[0].getStatusDetails().add(lastStatusDetailReport[0]);

                        }

                        lastStatusDetailReport[0].getContexts().add(new Context(statusDetail.getOrigin(), statusDetail.getExpectedMimeType()));


                    });
                }
            });

            final LinkcheckerDetailReport overallReport = new LinkcheckerDetailReport("Overall");

            final Map<Category, CategoryReport> categoryReports = new HashMap<Category, CategoryReport>();

            linkcheckerDetailReports.forEach(linkcheckerDetailReport -> {
                linkcheckerDetailReport.getCategoryReports().forEach(categoryReport -> {
                    categoryReport.getStatusDetails().stream().limit(1).findFirst().ifPresent(statusDetail -> {
                        categoryReports.computeIfAbsent(categoryReport.getCategory(), k -> {
                            CategoryReport overallCategoryReport = new CategoryReport(categoryReport.getCategory());
                            overallReport.getCategoryReports().add(overallCategoryReport);
                            return new CategoryReport();
                        }).getStatusDetails().add(statusDetail);
                    });
                });
            });

            linkcheckerDetailReports.add(overallReport);
        }

        return linkcheckerDetailReports;
    }
}
