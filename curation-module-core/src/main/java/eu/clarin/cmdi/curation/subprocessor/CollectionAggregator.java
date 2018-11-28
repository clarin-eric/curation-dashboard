package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.report.CollectionReport.FacetCollectionStruct;
import eu.clarin.cmdi.curation.report.CollectionReport.FacetReport;
import eu.clarin.cmdi.curation.report.CollectionReport.FileReport;
import eu.clarin.cmdi.curation.report.CollectionReport.HeaderReport;
import eu.clarin.cmdi.curation.report.CollectionReport.ResProxyReport;
import eu.clarin.cmdi.curation.report.CollectionReport.URLValidationReport;
import eu.clarin.cmdi.curation.report.CollectionReport.XMLPopulatedReport;
import eu.clarin.cmdi.curation.report.CollectionReport.XMLValidationReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.utils.TimeUtils;

/**
 * @author dostojic
 */
public class CollectionAggregator extends ProcessingStep<CMDCollection, CollectionReport> {

    private static final Logger _logger = LoggerFactory.getLogger(CollectionAggregator.class);

    private final int CHUNK_SIZE = 5000;

    @Override
    public void process(CMDCollection dir, final CollectionReport report){

        report.fileReport = new FileReport();
        report.headerReport = new HeaderReport();
        report.resProxyReport = new ResProxyReport();
        report.xmlPopulatedReport = new XMLPopulatedReport();
        report.xmlValidationReport = new XMLValidationReport();
        report.urlReport = new URLValidationReport();
        report.facetReport = new FacetReport();

        report.facetReport.facet = new ArrayList<>();
        
        for(String facetName : Configuration.FACETS) {
            FacetCollectionStruct facet = new FacetCollectionStruct();
            facet.name = facetName;
            facet.cnt = 0;
            report.facetReport.facet.add(facet);
        };

        //add info regarding file statistics
        report.fileReport.provider = dir.getPath().getFileName().toString();
        report.fileReport.numOfFiles = dir.getNumOfFiles();
        report.fileReport.size = dir.getSize();
        report.fileReport.minFileSize = dir.getMinFileSize();
        report.fileReport.maxFileSize = dir.getMaxFileSize();

        // process in portions to avoid memory thresholds
        Iterator<CurationEntity> it = dir.getChildren().iterator();
        int processed = 0;
        while (it.hasNext()) {

            final List<CurationEntity> chunk = new ArrayList<>(CHUNK_SIZE);
            for (int i = 0; i < CHUNK_SIZE && it.hasNext(); i++) {
                chunk.add(it.next());
            }

            long startTime = System.currentTimeMillis();
            
            chunk.parallelStream().forEach(entity -> {
                try {
                    entity.generateReport(report.getName());
                }
                catch (InterruptedException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                }
            });
            
/*            for (CurationEntity curationEntity : chunk) {
                curationEntity.generateReport(report.getName());
            }*/

            long end = System.currentTimeMillis();
            _logger.info("validation for {} files lasted {}", chunk.size(), TimeUtils.humanizeToTime(end - startTime));
            chunk.stream().forEach(child -> {
                try {
                    child.generateReport(report.getName()).mergeWithParent(report);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            processed += chunk.size();
            _logger.debug("{} records are processed so far, rest {}", processed, dir.getChildren().size() - processed);

        }

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
