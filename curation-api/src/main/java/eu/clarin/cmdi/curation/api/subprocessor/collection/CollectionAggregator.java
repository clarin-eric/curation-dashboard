package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport.RecordDetail;
import eu.clarin.cmdi.curation.api.report.collection.sec.FacetReport.FacetCollectionStruct;
import eu.clarin.cmdi.curation.api.report.collection.sec.HeaderReport.MDSelfLink;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport.Statistics;
import eu.clarin.cmdi.curation.api.report.collection.sec.ProfileReport.Profile;
import eu.clarin.cmdi.curation.api.report.collection.sec.ResProxyReport.InvalidReference;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.linkchecker.persistence.model.AggregatedStatus;
import eu.clarin.linkchecker.persistence.repository.AggregatedStatusRepository;
import eu.clarin.linkchecker.persistence.repository.UrlRepository;
import eu.clarin.linkchecker.persistence.utils.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * The type Collection aggregator.
 */
@Slf4j
@Component
@Scope("prototype")
public class CollectionAggregator {

    @Autowired
    private ApiConfig conf;
    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private AggregatedStatusRepository aRep;
    @Autowired
    private UrlRepository uRep;

    private final Map<String, Collection<String>> mdSelfLinks = new HashMap<String, Collection<String>>();

    private int counter;

    /**
     * Process.
     *
     * @param collection       the CMD collection which has the root path (directory) which contains the CMD instances of a collection
     * @param collectionReport the collection report the aggregated CMD instance reports are written to
     */
    @Transactional
    public void process(CMDCollection collection, CollectionReport collectionReport) {

        conf.getFacets()
                .forEach(facetName -> collectionReport.facetReport.facets.add(new FacetCollectionStruct(facetName)));

        collectionReport.fileReport.collectionRoot = conf.getDirectory().getDataRoot().relativize(collection.getPath()).toString();

        //ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(conf.getThreadpoolSize());
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        try {
            Files.walkFileTree(collection.getPath(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {

                    collectionReport.fileReport.numOfFiles++;

                    if (attrs.size() > collectionReport.fileReport.maxFileSize) {
                        collectionReport.fileReport.maxFileSize = attrs.size();
                    }
                    if (collectionReport.fileReport.minFileSize == 0
                            || attrs.size() < collectionReport.fileReport.minFileSize) {
                        collectionReport.fileReport.minFileSize = attrs.size();
                    }

                    collectionReport.fileReport.size += attrs.size();

                    final CMDInstance instance = ctx.getBean(CMDInstance.class, filePath, attrs.size(),
                            collectionReport.fileReport.provider);

                    executor.execute(() -> {

                        CMDInstanceReport instanceReport = null;
                        try {
                            instanceReport = instance.generateReport();
                        }
                        catch (MalFunctioningProcessorException e) {
                            executor.shutdownNow();
                            throw new RuntimeException(e);
                        }

                        addReport(collectionReport, instanceReport);

                    }); // end executor.execute

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    log.error("can't access file '{}'", file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e) {

            log.error("can't walk through path '{}'", collection.getPath());
            throw new RuntimeException(e);

        }

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        }
        catch (InterruptedException e) {
            log.error("Error occured while waiting for the threadpool to terminate.");
        }

        calculateAverages(collectionReport);
    }

    /**
     * Add report.
     *
     * @param collectionReport the collection report
     * @param instanceReport   the instance report
     */
    public synchronized void addReport(CollectionReport collectionReport, CMDInstanceReport instanceReport) {

        if (instanceReport.details.size() > 0) {//only add a record if there're details to report

            collectionReport.recordDetails.add(new RecordDetail(instanceReport.fileReport.location, instanceReport.details));
        }

        if (instanceReport.isProcessable) {

            if (instanceReport.instanceScore > collectionReport.maxScore) {
                collectionReport.maxScore = instanceReport.instanceScore;
            }

            if (instanceReport.instanceScore < collectionReport.minScore)
                collectionReport.minScore = instanceReport.instanceScore;

            // file
            collectionReport.fileReport.numOfFilesProcessable++;
            collectionReport.fileReport.aggregatedScore += instanceReport.fileReport.score;

            //profile
            collectionReport.profileReport.profiles.stream()
                    .filter(profile -> profile.schemaLocation.equals(instanceReport.profileHeaderReport.getSchemaLocation())).findFirst()
                    .ifPresentOrElse(profile -> profile.count++, () -> collectionReport.profileReport.profiles
                            .add(new Profile(instanceReport.profileHeaderReport.getSchemaLocation(), instanceReport.profileHeaderReport.getProfileHeader().isPublic(), instanceReport.profileScore)));
            collectionReport.profileReport.aggregatedScore += instanceReport.profileScore;

            // header
            if (instanceReport.instanceHeaderReport.schemaLocation != null) {
                collectionReport.headerReport.numWithSchemaLocation++;
            }
            if (instanceReport.instanceHeaderReport.isCRResident) {
                collectionReport.headerReport.numSchemaCRResident++;
            }
            if (instanceReport.instanceHeaderReport.mdProfile != null) {
                collectionReport.headerReport.numWithMdProfile++;
            }
            if (instanceReport.instanceHeaderReport.mdCollectionDisplayName != null) {
                collectionReport.headerReport.numWithMdCollectionDisplayName++;
            }
            if (instanceReport.instanceHeaderReport.mdSelfLink != null) {

                collectionReport.headerReport.numWithMdSelflink++;

                this.mdSelfLinks
                        .computeIfAbsent(instanceReport.instanceHeaderReport.mdSelfLink, k -> new ArrayList<String>())
                        .add(instanceReport.fileReport.location);

            }

            collectionReport.headerReport.aggregatedScore += instanceReport.instanceHeaderReport.score;

            // ResProxies
            collectionReport.resProxyReport.totNumOfResources += instanceReport.resProxyReport.numOfResources;
            collectionReport.resProxyReport.totNumOfResourcesWithMime += instanceReport.resProxyReport.numOfResourcesWithMime;
            collectionReport.resProxyReport.totNumOfResourcesWithReference += instanceReport.resProxyReport.numOfResourcesWithReference;

            if (instanceReport.resProxyReport.invalidReferences.size() > 0) {
                collectionReport.resProxyReport.invalidReferences.add(new InvalidReference(
                        instanceReport.fileReport.location, instanceReport.resProxyReport.invalidReferences));
            }
            collectionReport.resProxyReport.aggregatedScore += instanceReport.resProxyReport.score;

            // XmlPopulation
            collectionReport.xmlPopulationReport.totNumOfXMLElements += instanceReport.xmlPopulationReport.numOfXMLElements;
            collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements += instanceReport.xmlPopulationReport.numOfXMLSimpleElements;
            collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements += instanceReport.xmlPopulationReport.numOfXMLEmptyElements;
            collectionReport.xmlPopulationReport.aggregatedScore += instanceReport.xmlPopulationReport.score;

            // XmlValidation
            collectionReport.xmlValidityReport.totNumOfValidRecords += instanceReport.xmlValidityReport.score;
            collectionReport.xmlValidityReport.aggregatedScore += instanceReport.xmlValidityReport.score;

            // Facet
            instanceReport.facetReport.coverages
                    .stream()
                    .filter(coverage -> coverage.coveredByInstance)
                    .forEach(instanceFacet -> collectionReport.facetReport.facets
                            .stream()
                            .filter(collectionFacet -> collectionFacet.name.equals(instanceFacet.name))
                            .findFirst()
                            .get()
                            .count++
                    );
            collectionReport.facetReport.aggregatedScore += instanceReport.facetReport.score;
        } else {
            collectionReport.fileReport.numOfFilesNonProcessable++;
        }
    }

    private void calculateAverages(CollectionReport collectionReport) {
        // find all mdSelfLinks which appear in more than origin

        this.mdSelfLinks.entrySet()
                .stream()
                .filter(entrySet -> entrySet.getValue().size() > 1)
                .forEach(entrySet -> collectionReport.headerReport.duplicatedMDSelfLink.add(new MDSelfLink(entrySet.getKey(), entrySet.getValue())));

        // lincheckerReport

        try (Stream<AggregatedStatus> stream = aRep.findAllByProvidergroupName(collectionReport.getName())) {

            stream.forEach(categoryStats -> {
                Statistics xmlStatistics = new Statistics(categoryStats.getCategory());
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

            collectionReport.linkcheckerReport.aggregatedScore = (double) collectionReport.linkcheckerReport.ratioOfValidLinks
                    * collectionReport.fileReport.numOfFilesProcessable
                    * collectionReport.linkcheckerReport.totNumOfCheckedLinks / (double) collectionReport.linkcheckerReport.totNumOfLinks;
        }

        collectionReport.fileReport.aggregatedMaxScore = eu.clarin.cmdi.curation.api.report.instance.sec.FileReport.maxScore
                * collectionReport.fileReport.numOfFiles;

        collectionReport.profileReport.aggregatedMaxScore = eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport.maxScore
                * collectionReport.fileReport.numOfFilesProcessable;

        collectionReport.headerReport.aggregatedMaxScore = eu.clarin.cmdi.curation.api.report.instance.sec.InstanceHeaderReport.maxScore
                * collectionReport.fileReport.numOfFilesProcessable;

        collectionReport.resProxyReport.aggregatedMaxScore = eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport.maxScore
                * collectionReport.fileReport.numOfFilesProcessable;

        collectionReport.xmlPopulationReport.aggregatedMaxScore = eu.clarin.cmdi.curation.api.report.instance.sec.XmlPopulationReport.maxScore
                * collectionReport.fileReport.numOfFilesProcessable;

        collectionReport.xmlValidityReport.aggregatedMaxScore = eu.clarin.cmdi.curation.api.report.instance.sec.XmlValidityReport.maxScore
                * collectionReport.fileReport.numOfFilesProcessable;

        collectionReport.facetReport.aggregatedMaxScore = eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.maxScore
                * collectionReport.fileReport.numOfFilesProcessable;


        if (collectionReport.fileReport.numOfFiles > 0) {
            // file
            collectionReport.fileReport.avgFileSize = collectionReport.fileReport.size
                    / collectionReport.fileReport.numOfFiles;
            collectionReport.fileReport.scorePercentage = collectionReport.fileReport.aggregatedScore
                    / (double) collectionReport.fileReport.numOfFiles;
            collectionReport.fileReport.avgScore = (double) (collectionReport.fileReport.aggregatedScore
                    / collectionReport.fileReport.numOfFiles);
            //profile
            collectionReport.profileReport.totNumOfProfiles = collectionReport.profileReport.profiles.size();

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
            // file

            //profile
            collectionReport.profileReport.avgScore = collectionReport.profileReport.aggregatedScore
                    / (double) collectionReport.fileReport.numOfFilesProcessable;
            collectionReport.profileReport.scorePercentage = collectionReport.profileReport.aggregatedScore
                    / (double) collectionReport.profileReport.aggregatedMaxScore;

            // header
            collectionReport.headerReport.scorePercentage = collectionReport.headerReport.aggregatedScore
                    / (double) collectionReport.headerReport.aggregatedMaxScore;
            collectionReport.headerReport.avgScore = (collectionReport.headerReport.aggregatedScore
                    / (double) collectionReport.fileReport.numOfFilesProcessable);
            // resProxy
            collectionReport.resProxyReport.avgNumOfResources = (collectionReport.resProxyReport.totNumOfResources
                    / (double) collectionReport.fileReport.numOfFilesProcessable);
            collectionReport.resProxyReport.avgNumOfResourcesWithMime = (collectionReport.resProxyReport.totNumOfResourcesWithMime
                    / (double) collectionReport.fileReport.numOfFilesProcessable);
            collectionReport.resProxyReport.avgNumOfResourcesWithReference = (collectionReport.resProxyReport.totNumOfResourcesWithReference
                    / (double) collectionReport.fileReport.numOfFilesProcessable);
            collectionReport.resProxyReport.scorePercentage = collectionReport.resProxyReport.aggregatedScore
                    / collectionReport.resProxyReport.aggregatedMaxScore;
            collectionReport.resProxyReport.avgScore = (collectionReport.resProxyReport.aggregatedScore
                    / (double) collectionReport.fileReport.numOfFilesProcessable);
            // XmlPopulation
            collectionReport.xmlPopulationReport.avgNumOfXMLElements = collectionReport.xmlPopulationReport.totNumOfXMLElements
                    / (double) collectionReport.fileReport.numOfFilesProcessable;
            collectionReport.xmlPopulationReport.avgNumOfXMLSimpleElements = collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements
                    / (double) collectionReport.fileReport.numOfFilesProcessable;
            collectionReport.xmlPopulationReport.avgNumOfXMLEmptyElements = collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements
                    / collectionReport.fileReport.numOfFilesProcessable;

            if (collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements > 0) {
                collectionReport.xmlPopulationReport.avgRateOfPopulatedElements = (1.0
                        - collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements
                        / (double) collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements);
            }
            collectionReport.xmlPopulationReport.scorePercentage = collectionReport.xmlPopulationReport.aggregatedScore
                    / (double) collectionReport.xmlPopulationReport.aggregatedMaxScore;
            collectionReport.xmlPopulationReport.avgScore = (collectionReport.xmlPopulationReport.aggregatedScore
                    / (double) collectionReport.fileReport.numOfFilesProcessable);
            // xmlvalidity
            collectionReport.xmlValidityReport.scorePercentage = collectionReport.xmlValidityReport.aggregatedScore
                    / (double) collectionReport.xmlValidityReport.aggregatedMaxScore;
            collectionReport.xmlValidityReport.avgScore = (collectionReport.xmlValidityReport.aggregatedScore
                    / (double) collectionReport.fileReport.numOfFilesProcessable);
            // facet
            collectionReport.facetReport.facets
                    .forEach(facet -> facet.avgCoverage = facet.count / (double) collectionReport.fileReport.numOfFilesProcessable);
            collectionReport.facetReport.percCoverageNonZero = (double) collectionReport.facetReport.facets.stream()
                    .filter(facet -> facet.count > 0).count() / collectionReport.facetReport.facets.size();
            collectionReport.facetReport.scorePercentage = collectionReport.facetReport.aggregatedScore
                    / (double) collectionReport.facetReport.aggregatedMaxScore;
            collectionReport.facetReport.avgScore = (collectionReport.facetReport.aggregatedScore
                    / (double) collectionReport.fileReport.numOfFilesProcessable);
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
                    / (double) collectionReport.linkcheckerReport.aggregatedMaxScore;
            //collection
            collectionReport.avgScore = collectionReport.aggregatedScore
                    / (double) collectionReport.fileReport.numOfFilesProcessable;

        }
    }
}
