package eu.clarin.cmdi.curation.processor;

import java.util.Collection;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.subprocessor.ProcessingActivity;

public abstract class AbstractProcessor {

    public AbstractProcessor() {
    }

    public void process(CurationEntity entity) {
	for (ProcessingActivity activity : createPipeline())
	    // stop applying further processors in case of fatal error
	    if (activity.process(entity) == Severity.FATAL){
		entity.setNumOfValidFiles(0);//file/dir not valid
		return;
	    }
    }

    protected abstract Collection<ProcessingActivity> createPipeline();

}