package eu.clarin.cmdi.curation.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.entities.CMDInstance;

import eu.clarin.cmdi.curation.exception.ProfileNotFoundException;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.subprocessor.XMLValidator;
import eu.clarin.cmdi.curation.subprocessor.URLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.ErrorReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public abstract class AbstractProcessor<R extends Report<?>> {

    private static final Logger _logger = LoggerFactory.getLogger(AbstractProcessor.class);

    public Report<?> process(CurationEntity entity, String parentName) {


        Report<?> report = createReport();

        try {
            for (ProcessingStep step : createPipeline()) {


                if (step instanceof URLValidator) {
                    URLValidator urlValidator = (URLValidator) step;

                    urlValidator.process((CMDInstance) entity, (CMDInstanceReport) report, parentName);
                } else {
                    step.process(entity, report);
                }

                if (step instanceof XMLValidator) {
                    report.addSegmentScore(((XMLValidator) step).calculateValidityScore());
                }


                if (!(step instanceof URLValidator) || Configuration.HTTP_VALIDATION) {
                    report.addSegmentScore(step.calculateScore(report));
                }

                //logging decreases performance
//                _logger.info("Processed Record: " + entity.toString() + ", step: " + step.getClass().getSimpleName());

            }

            return report;
        } catch (VTDException | ExecutionException | TransformerException | IOException | FileSizeException | SAXException | ParserConfigurationException | ProfileNotFoundException e) {
            _logger.error("There was an error processing record: " + entity.toString() + " : " + e.getMessage());
            return new ErrorReport(report.getName(), e.getMessage());
        }

    }

    protected abstract Collection<ProcessingStep> createPipeline();

    protected abstract R createReport();

    private String getStackTrace(final Throwable ex) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        ex.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

}