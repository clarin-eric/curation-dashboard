package eu.clarin.cmdi.curation.api.cache;

import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.processor.CMDCollectionProcessor;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.subprocessor.collection.CollectionAggregator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
public class CollectionReportCache {

    private final ApplicationContext applicationContext;

    public CollectionReportCache(ApplicationContext applicationContext) {

        this.applicationContext = applicationContext;
    }

    @Cacheable(value = "collectionReportCache", key = "#collection.path.toString()")
    public CollectionReport getCollectionReport(CMDCollection collection) {

        return null;
    }

    @CachePut(value = "collectionReportCache", key = "#collection.path.toString()")
    public CollectionReport getNewCollectionReport(CMDCollection collection) {

        LocalDateTime start = LocalDateTime.now();

        CollectionReport report = new CollectionReport();

        log.info("Started report generation for collection: " + collection.getPath());

        if (collection.getPath().getNameCount() >= 1) {

            String providerName = collection.getPath().getName(collection.getPath().getNameCount() - 1).toString();

            report.fileReport.provider = providerName;
            applicationContext.getBean(CollectionAggregator.class).process(collection, report);

            LocalDateTime end = LocalDateTime.now();

            log.info("It took {} to generate the report for collection: {}", Duration.between(start, end).toString(), report.getName());
        }
        else {
            log.error("the provider group is the last name in the path. Therefore the collection path MUSTN'T be the root directory");
        }
        return report;
    }
}
