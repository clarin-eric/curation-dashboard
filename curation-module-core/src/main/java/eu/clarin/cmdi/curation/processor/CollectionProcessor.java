package eu.clarin.cmdi.curation.processor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.exception.ProfileNotFoundException;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.report.ErrorReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.subprocessor.CollectionAggregator;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**

 */
public class CollectionProcessor extends AbstractProcessor<CollectionReport> {

    private static final Logger _logger = LoggerFactory.getLogger(CollectionProcessor.class);

    @Override
    public Report<?> process(CurationEntity entity, String parentName) {

        Report<?> report = createReport();


        for (ProcessingStep step : createPipeline()) {

            try {
                step.process(entity, report);

                report.addSegmentScore(step.calculateScore(report));

            } catch (FileSizeException | ExecutionException | IOException | VTDException | TransformerException | SAXException | ParserConfigurationException | ProfileNotFoundException e) {
                _logger.error("Exception when processing " + step.toString() + " : " + e.getMessage());
                //if it is not a collection report, keep the loop going for the following records, dont just produce an error report
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
