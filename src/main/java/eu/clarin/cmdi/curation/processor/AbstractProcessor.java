package eu.clarin.cmdi.curation.processor;

import java.util.Collection;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.subprocessor.CurationTask;

	public abstract class AbstractProcessor{
		
	public AbstractProcessor() {
	}

	public void process(CurationEntity entity) {
		for(CurationTask task: createPipeline())		
			if(task.process(entity) == Severity.FATAL)//stop applying further processors in case of fatal curation error
				return;	
		entity.setValid(true);
		
	}
	
	protected abstract Collection<CurationTask> createPipeline();
	
	

}