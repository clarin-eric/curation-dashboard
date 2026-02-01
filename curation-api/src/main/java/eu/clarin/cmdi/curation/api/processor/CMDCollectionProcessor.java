package eu.clarin.cmdi.curation.api.processor;

import eu.clarin.cmdi.curation.api.cache.CollectionReportCache;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.subprocessor.collection.CollectionLinkchecker;
import eu.clarin.cmdi.curation.api.subprocessor.collection.CollectionScoreCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class CMDCollectionProcessor {

    private final ApiConfig conf;
    private final ApplicationContext applicationContext;
    private final CollectionReportCache collectionReportCache;



    public CollectionReport process(CMDCollection collection) {

        CollectionReport collectionReport = this.collectionReportCache.getNewCollectionReport(collection);

        applicationContext.getBean(CollectionLinkchecker.class).process(collectionReport);
        applicationContext.getBean(CollectionScoreCalculator.class).process(collectionReport);

        return collectionReport;
    }
}
