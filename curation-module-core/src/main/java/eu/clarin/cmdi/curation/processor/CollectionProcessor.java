package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.report.ErrorReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.subprocessor.CollectionAggregator;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dostojic
 */
public class CollectionProcessor extends AbstractProcessor<CollectionReport> {

    private static final Logger logger = LoggerFactory.getLogger(CollectionProcessor.class);

    @Override
    public Report<?> process(CurationEntity entity, String parentName) {

        Report<?> report = createReport();


        for (ProcessingStep step : createPipeline()) {

            try {
                step.process(entity, report);

                report.addSegmentScore(step.calculateScore(report));

            } catch (FileSizeException e) {
                //if it is not a collection report, keep the loop going for the following records, dont just produce an error report
                if (!(report instanceof CollectionReport)) {
                    return new ErrorReport(report.getName(), e.getMessage());
                } else {
                    addInvalidFile(report, e);
                }
            } catch (Exception e) {
                logger.error("Exception: " + e.getMessage());
                //if it is a collection report, keep the loop going for the following records, dont just produce an error report
                if (!(report instanceof CollectionReport)) {
                    return new ErrorReport(report.getName(), e.getMessage());
                } else {
                    addInvalidFile(report, e);
                }
            }
        }

        return report;


    }

    private void addInvalidFile(Report<?> report, Exception e) {
        CollectionReport.InvalidFile invalidFile = new CollectionReport.InvalidFile();
        invalidFile.recordName = e.getMessage();
        invalidFile.reason = e.getCause().getMessage();
        ((CollectionReport) report).addInvalidFile(invalidFile);
    }

    @Override
    protected Collection<ProcessingStep> createPipeline() {
        return Arrays.asList(new CollectionAggregator());
    }

    @Override
    protected CollectionReport createReport() {
        return new CollectionReport();
    }

}
