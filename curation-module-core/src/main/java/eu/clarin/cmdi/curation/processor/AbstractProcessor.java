package eu.clarin.cmdi.curation.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;


import eu.clarin.cmdi.curation.entities.CMDInstance;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.subprocessor.InstanceXMLValidator;
import eu.clarin.cmdi.curation.subprocessor.URLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.ErrorReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;

public abstract class AbstractProcessor<R extends Report<?>> {

    private static final Logger _logger = LoggerFactory.getLogger(AbstractProcessor.class);

    public Report<?> process(CurationEntity entity, String parentName) throws InterruptedException {


        Report<?> report = createReport();

        try {
            for (ProcessingStep step : createPipeline()) {



                if(step instanceof URLValidator){
                    URLValidator urlValidator = (URLValidator)step;

                    urlValidator.process((CMDInstance) entity, (CMDInstanceReport) report, parentName);
//                    ((URLValidator)step).process(entity, report, parentName);
                }else{
                    step.process(entity, report);
                }


                _logger.info("processed Record: " + entity.toString() + ", step: " + step.getClass().getSimpleName());

                if (step instanceof InstanceXMLValidator) {
                    report.addSegmentScore(((InstanceXMLValidator) step).calculateValidityScore());
                }


                if (!(step instanceof URLValidator) || Configuration.HTTP_VALIDATION) {
                    report.addSegmentScore(step.calculateScore(report));
                }


            }

            return report;
        } catch (FileSizeException e) {
            _logger.error(e.getMessage());
            return new ErrorReport(report.getName(), e.getMessage());
        } catch (Exception e) {
            _logger.error("", e);
            String message = e.getMessage();
            message = message.replace(" java.lang.Exception","");
            if(message==null || message.isEmpty()){
                message = "There was an unknown error. Please report it.";
            }

            _logger.error("", e);

            return new ErrorReport(report.getName(), message);
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