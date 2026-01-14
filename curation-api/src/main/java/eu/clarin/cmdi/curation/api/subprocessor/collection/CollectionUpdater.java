package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.cmdi.curation.api.CurationModule;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.linkchecker.persistence.model.AggregatedStatus;
import eu.clarin.linkchecker.persistence.repository.AggregatedStatusRepository;
import eu.clarin.linkchecker.persistence.repository.UrlRepository;
import eu.clarin.linkchecker.persistence.utils.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Slf4j
@Component
public class CollectionUpdater {

    private final ApplicationContext ctx;
    private final AggregatedStatusRepository aRep;
    private final UrlRepository uRep;

    public CollectionUpdater(ApplicationContext ctx, AggregatedStatusRepository aRep, UrlRepository uRep) {
        this.ctx = ctx;
        this.aRep = aRep;
        this.uRep = uRep;
    }
    @Transactional
    public void process(CollectionReport collectionReport) {

        collectionReport.creationTime = LocalDateTime.now();

        collectionReport.linkcheckerReport = new LinkcheckerReport();

        // lincheckerReport

        try (Stream<AggregatedStatus> stream = aRep.findAllByProvidergroupName(collectionReport.getName())) {

            stream.forEach(categoryStats -> {
                LinkcheckerReport.Statistics xmlStatistics = new LinkcheckerReport.Statistics(categoryStats.getCategory());
                xmlStatistics.avgRespTime = categoryStats.getAvgDuration();
                xmlStatistics.maxRespTime = categoryStats.getMaxDuration();
                xmlStatistics.count = categoryStats.getNumberId();
                collectionReport.linkcheckerReport.totNumOfCheckedLinks += categoryStats.getNumberId().intValue();
                collectionReport.linkcheckerReport.totNumOfLinksWithDuration += categoryStats.getNumberDuration().intValue();

                collectionReport.linkcheckerReport.statistics.add(xmlStatistics);
            });
        }
        catch (IllegalArgumentException ex) {

            log.error(ex.getMessage());

        }
        catch (Exception ex) {

            log.error("couldn't get category statistics for provider group '{}' from database", collectionReport.getName(),
                    ex);
        }

        collectionReport.linkcheckerReport.totNumOfLinks = (int) uRep
                .countByProvidergroupName(collectionReport.getName());
        collectionReport.linkcheckerReport.totNumOfUniqueLinks = (int) uRep
                .countDistinctByProvidergroupName(collectionReport.getName());

        if (collectionReport.linkcheckerReport.totNumOfCheckedLinks > 0) {
            collectionReport.linkcheckerReport.statistics.stream().filter(statistics -> statistics.category == Category.Ok)
                    .findFirst().ifPresent(
                            statistics -> collectionReport.linkcheckerReport.ratioOfValidLinks = statistics.count
                                    / (double) collectionReport.linkcheckerReport.totNumOfCheckedLinks
                    );

        }

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
                    .filter(statistics -> statistics.maxRespTime != null).mapToLong(statistics -> statistics.maxRespTime)
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
            collectionReport.profileReport.profiles.forEach(profile -> this.ctx.getBean(CurationModule.class)
                .processCMDProfile(profile.schemaLocation)
                .collectionUsage
                .add(new CMDProfileReport.CollectionUsage(collectionReport.fileReport.provider, profile.count))
            );
        }
    }
}
