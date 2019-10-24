package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.report.CollectionReport.*;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**

 */
public class CollectionAggregator extends ProcessingStep<CMDCollection, CollectionReport> {

    private static final Logger _logger = LoggerFactory.getLogger(CollectionAggregator.class);

    @Override
    public void process(CMDCollection dir, final CollectionReport report) {

        report.fileReport = new FileReport();
        report.headerReport = new HeaderReport();
        report.resProxyReport = new ResProxyReport();
        report.xmlPopulatedReport = new XMLPopulatedReport();
        report.xmlValidationReport = new XMLValidationReport();
        report.urlReport = new URLValidationReport();
        report.facetReport = new FacetReport();

        report.facetReport.facet = new ArrayList<>();

        for (String facetName : Configuration.FACETS) {
            FacetCollectionStruct facet = new FacetCollectionStruct();
            facet.name = facetName;
            facet.cnt = 0;
            report.facetReport.facet.add(facet);
        }
        ;

        //add info regarding file statistics
        report.fileReport.provider = dir.getPath().getFileName().toString();
        report.fileReport.numOfFiles = dir.getNumOfFiles();
        report.fileReport.size = dir.getSize();
        report.fileReport.minFileSize = dir.getMinFileSize();
        report.fileReport.maxFileSize = dir.getMaxFileSize();
        
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Configuration.THREAD_POOL_SIZE);
        
        
        while(!dir.getChildren().isEmpty()) {
            
            CurationEntity entity = dir.getChildren().pop();

            executor.submit(() -> 
                {
                    entity.generateReport(report.getName()).mergeWithParent(report);

                });
        }
        
        executor.shutdown();
        
        while(!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex) {
                _logger.error("on error occured while waiting for the treadpool to terminate");
            }
        };

        report.calculateAverageValues();

        if (!CMDInstance.duplicateMDSelfLink.isEmpty()) {
            report.headerReport.duplicatedMDSelfLink = CMDInstance.duplicateMDSelfLink;
        }
        CMDInstance.duplicateMDSelfLink.clear();
        CMDInstance.mdSelfLinks.clear();

    }

    @Override
    public Score calculateScore(CollectionReport report) {
        double score = report.fileReport.numOfFiles;
        if (report.file != null) {
            report.file.forEach(ir -> addMessage(Severity.ERROR, "Invalid file:" + ir.recordName + ", reason: " + ir.reason));
            score = (score - report.file.size()) / score;
        }

        return new Score(score, (double) report.fileReport.numOfFiles, "invalid-files", msgs);
    }

}
