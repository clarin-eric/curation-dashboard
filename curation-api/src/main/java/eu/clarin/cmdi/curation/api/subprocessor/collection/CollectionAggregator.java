package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.cmdi.curation.api.CurationModule;
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
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * The type Collection aggregator.
 */
@Slf4j
@Component
@Scope("prototype")
public class CollectionAggregator {

    private final ApiConfig conf;
    private final ApplicationContext ctx;


    private final Map<String, Collection<String>> mdSelfLinks = new HashMap<>();

    private final Map<String, AtomicInteger> profileUsage = new HashMap<>();

    private final Lock lock = new ReentrantLock();

    public CollectionAggregator(ApiConfig conf, ApplicationContext ctx) {
        this.conf = conf;
        this.ctx = ctx;

    }

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

        final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        //
        final Semaphore maxInQueue = new Semaphore(conf.getMaxInQueue());

        try {
            Files.walkFileTree(collection.getPath(), new FileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {

                    try {
                        maxInQueue.acquire();
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

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


                        CMDInstanceReport instanceReport;
                        try {
                            instanceReport = instance.generateReport();
                        }
                        catch (MalFunctioningProcessorException e) {
                            executor.shutdownNow();
                            throw new RuntimeException(e);
                        }
                        finally {
                            maxInQueue.release();
                        }
                        CollectionAggregator.this.lock.lock();
                        addReport(collectionReport, instanceReport);

                    }); // end executor.execute

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.error("can't access file '{}'", file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

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
            log.error("Error occurred while waiting for the thread pool to terminate.");
        }

        // find all mdSelfLinks which appear in more than origin

        this.mdSelfLinks.forEach((key, value) -> {
            if (value.size() == 1) { //link is unique
                collectionReport.headerReport.numWithUniqueMdSelflink++;
            } else {//duplicate link
                collectionReport.headerReport.duplicatedMDSelfLink.add(new MDSelfLink(key, value));
            }
        });
        // adding score for unique selflinks
        collectionReport.headerReport.aggregatedScore += collectionReport.headerReport.numWithUniqueMdSelflink;
    }

    /**
     * Add report.
     *
     * @param collectionReport the collection report
     * @param instanceReport   the instance report
     */
    public void addReport(CollectionReport collectionReport, CMDInstanceReport instanceReport) {

        try {
            if (!instanceReport.details.isEmpty()) {//only add a record if there are details to report

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
                        .filter(profile -> profile.schemaLocation.equals(instanceReport.profileHeaderReport.getSchemaLocation()))
                        .findFirst()
                        .ifPresentOrElse(profile -> profile.count++, () -> collectionReport.profileReport.profiles
                                .add(
                                        new Profile(
                                                instanceReport.profileHeaderReport.getSchemaLocation(),
                                                instanceReport.profileHeaderReport.getProfileHeader().isPublic(),
                                                instanceReport.profileHeaderReport.getProfileHeader().isCrResident(),
                                                instanceReport.profileScore)
                                )
                        );
                collectionReport.profileReport.aggregatedScore += instanceReport.profileScore;

                // header
                if (instanceReport.instanceHeaderReport.schemaLocation != null) {
                    collectionReport.headerReport.numWithSchemaLocation++;
                }
                if (instanceReport.instanceHeaderReport.isCrResident) {
                    collectionReport.headerReport.numSchemaCrResident++;
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
                            .computeIfAbsent(instanceReport.instanceHeaderReport.mdSelfLink, k -> new ArrayList<>())
                            .add(instanceReport.fileReport.location);

                }

                collectionReport.headerReport.aggregatedScore += instanceReport.instanceHeaderReport.score;

                // ResProxies
                collectionReport.resProxyReport.totNumOfResources += instanceReport.resProxyReport.numOfResources;
                collectionReport.resProxyReport.totNumOfResourcesWithMime += instanceReport.resProxyReport.numOfResourcesWithMime;
                collectionReport.resProxyReport.totNumOfResourcesWithReference += instanceReport.resProxyReport.numOfResourcesWithReference;

                if (!instanceReport.resProxyReport.invalidReferences.isEmpty()) {
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
                collectionReport.xmlValidityReport.totNumOfValidRecords += (int) instanceReport.xmlValidityReport.score;
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

                this.profileUsage.computeIfAbsent(instanceReport.profileHeaderReport.getSchemaLocation(), k -> new AtomicInteger()).incrementAndGet();
            }
            else {
                collectionReport.fileReport.numOfFilesNonProcessable++;
            }
        }
        finally {
            this.lock.unlock();
        }
    }
}
