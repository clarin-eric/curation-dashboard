package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.cmdi.curation.api.CurationModule;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
public class CollectionScoreCalculator {

    private final ApplicationContext applicationContext;

    public CollectionScoreCalculator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Transactional
    public void process(CollectionReport collectionReport) {

        if (collectionReport.linkcheckerReport.totNumOfLinks > 0) {
            collectionReport.linkcheckerReport.aggregatedMaxScore = collectionReport.fileReport.numOfFilesProcessable
                    * collectionReport.linkcheckerReport.totNumOfCheckedLinks / (double) collectionReport.linkcheckerReport.totNumOfLinks;

            collectionReport.linkcheckerReport.aggregatedScore = collectionReport.linkcheckerReport.ratioOfValidLinks
                    * collectionReport.fileReport.numOfFilesProcessable
                    * collectionReport.linkcheckerReport.totNumOfCheckedLinks / (double) collectionReport.linkcheckerReport.totNumOfLinks;
        }

        if (collectionReport.fileReport.numOfFiles > 0) {

            //collection
            collectionReport.aggregatedScore = collectionReport.fileReport.aggregatedScore + collectionReport.profileReport.aggregatedScore
                    + collectionReport.headerReport.aggregatedScore + collectionReport.resProxyReport.aggregatedScore
                    + collectionReport.xmlPopulationReport.aggregatedScore + collectionReport.xmlValidityReport.aggregatedScore
                    + collectionReport.linkcheckerReport.aggregatedScore + collectionReport.facetReport.aggregatedScore;

            collectionReport.aggregatedMaxScore = collectionReport.fileReport.aggregatedMaxScore + collectionReport.profileReport.aggregatedMaxScore
                    + collectionReport.headerReport.aggregatedMaxScore + collectionReport.resProxyReport.aggregatedMaxScore
                    + collectionReport.xmlPopulationReport.aggregatedMaxScore + collectionReport.xmlValidityReport.aggregatedMaxScore
                    + collectionReport.linkcheckerReport.aggregatedMaxScore + collectionReport.facetReport.aggregatedMaxScore;

            collectionReport.scorePercentage = collectionReport.aggregatedScore / collectionReport.aggregatedMaxScore;

        }
        if (collectionReport.fileReport.numOfFilesProcessable > 0) {
            // linkchecker
            collectionReport.linkcheckerReport.avgNumOfLinks = collectionReport.linkcheckerReport.totNumOfLinks
                    / (double) collectionReport.fileReport.numOfFilesProcessable;
            collectionReport.linkcheckerReport.avgNumOfUniqueLinks = collectionReport.linkcheckerReport.totNumOfUniqueLinks
                    / (double) collectionReport.fileReport.numOfFilesProcessable;
            collectionReport.linkcheckerReport.maxRespTime = collectionReport.linkcheckerReport.statistics.stream()
                    .filter(statistics -> statistics.maxRespTime != null).mapToInt(statistics -> statistics.maxRespTime)
                    .max().orElse(0);
            if (collectionReport.linkcheckerReport.totNumOfLinksWithDuration > 0) {
                collectionReport.linkcheckerReport.avgRespTime = collectionReport.linkcheckerReport.statistics.stream()
                        .filter(statistics -> statistics.avgRespTime != null)
                        .mapToDouble(statistics -> statistics.avgRespTime * statistics.count).sum()
                        / (double) collectionReport.linkcheckerReport.totNumOfLinksWithDuration;
            }
            collectionReport.linkcheckerReport.scorePercentage = collectionReport.linkcheckerReport.aggregatedScore
                    / collectionReport.linkcheckerReport.aggregatedMaxScore;
            //collection
            collectionReport.avgScore = collectionReport.aggregatedScore
                    / (double) collectionReport.fileReport.numOfFilesProcessable;

            // add profileUsage
            collectionReport.profileReport.profiles.forEach(profile -> this.applicationContext.getBean(CurationModule.class)
                .processCMDProfile(profile.schemaLocation)
                .collectionUsage
                .add(new CMDProfileReport.CollectionUsage(collectionReport.fileReport.provider, profile.count))
            );
        }
    }
}
