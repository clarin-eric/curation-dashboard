package eu.clarin.cmdi.curation.processor;

import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.subprocessor.CollectionAggregator;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionProcessor {

    private static final Logger _logger = LoggerFactory.getLogger(CollectionProcessor.class);

    public CollectionReport process(CMDCollection collection) {

        long start = System.currentTimeMillis();

        CollectionReport report = new CollectionReport();
        _logger.info("Started report generation for collection: " + collection.getPath());

        CollectionAggregator collectionAggregator = null;
        try {
            collectionAggregator = new CollectionAggregator();

            collectionAggregator.process(collection, report);
            report.addSegmentScore(collectionAggregator.calculateScore(report));


        } catch (Exception e) {
            _logger.error("Exception when processing " + collectionAggregator.toString() + " : " + e.getMessage());
            addInvalidFile(report, e);
        }

        long end = System.currentTimeMillis();
        _logger.info("It took " + TimeUtils.humanizeToTime(end - start) + " to generate the report for collection: " + report.getName());

        return report;
    }

    private void addInvalidFile(CollectionReport report, Exception e) {
        CollectionReport.InvalidFile invalidFile = new CollectionReport.InvalidFile();
        invalidFile.recordName = e.getMessage();
        invalidFile.reason = e.getCause().getMessage();
        report.addInvalidFile(invalidFile);
    }

}
