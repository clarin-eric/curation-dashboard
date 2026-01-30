package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport;
import eu.clarin.linkchecker.persistence.model.AggregatedStatus;
import eu.clarin.linkchecker.persistence.model.UrlCount;
import eu.clarin.linkchecker.persistence.repository.StatusRepository;
import eu.clarin.linkchecker.persistence.repository.UrlRepository;
import eu.clarin.linkchecker.persistence.utils.Category;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
@Scope("prototype")
@Lazy
public class CollectionLinkchecker {

    private final PlatformTransactionManager transactionManager;

    private final StatusRepository statusRepository;
    private final UrlRepository urlRepository;

    private final Map<String, LinkcheckerReport> linkcheckerReports;

    public CollectionLinkchecker(PlatformTransactionManager transactionManager, StatusRepository statusRepository, UrlRepository urlRepository) {
        this.transactionManager = transactionManager;

        this.statusRepository = statusRepository;
        this.urlRepository = urlRepository;

        this.linkcheckerReports = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        // must call transaction programmatically since @Transactional doesn't work with @PostConstruct
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            try(Stream<AggregatedStatus> stream = statusRepository.findAggregatedStatus()){

                stream.forEach(aggregatedStatus -> {

                    LinkcheckerReport.Statistics  statistic = new LinkcheckerReport.Statistics(aggregatedStatus.getCategory());
                    statistic.avgRespTime = aggregatedStatus.getAvgDuration();
                    statistic.maxRespTime = aggregatedStatus.getMaxDuration();
                    statistic.count = aggregatedStatus.getNumberId();
                    statistic.nonNullCount = aggregatedStatus.getNumberDuration();

                    LinkcheckerReport linkcheckerReport = this.linkcheckerReports.computeIfAbsent(aggregatedStatus.getProvidergroupName(), k -> new LinkcheckerReport());
                    linkcheckerReport.statistics.add(statistic);
                    linkcheckerReport.totNumOfCheckedLinks += statistic.count;
                });
            }

            try(Stream<UrlCount> stream = urlRepository.aggregateCountUrl()){

                stream.forEach(urlCount -> {

                    LinkcheckerReport linkcheckerReport = this.linkcheckerReports.computeIfAbsent(urlCount.getProvidergroupName(), k -> new LinkcheckerReport());

                    linkcheckerReport.totNumOfLinks = urlCount.getCount().intValue();
                    linkcheckerReport.totNumOfUniqueLinks = urlCount.getDistinctCount().intValue();
                    // calculating the ratio of valid links
                    if (linkcheckerReport.totNumOfCheckedLinks > 0) {
                        linkcheckerReport.statistics.stream().filter(statistics -> statistics.category == Category.Ok)
                            .findFirst()
                            .ifPresent(
                        statistics -> linkcheckerReport.ratioOfValidLinks = statistics.count
                                / (double) linkcheckerReport.totNumOfCheckedLinks
                            );
                    }
                });
            }
        });
    }

    public void process(CollectionReport collectionReport) {

        collectionReport.linkcheckerReport = this.linkcheckerReports.get(collectionReport.getName());
    }
}
