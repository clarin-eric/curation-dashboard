package eu.clarin.cmdi.curation.processor;

import java.util.Collection;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.subprocessor.CurationStep;

	public abstract class AbstractProcessor{
	
	protected final Collection<CurationStep> pipeline;
		
	public AbstractProcessor() {
		this.pipeline = initPipeline();
	}

	public void process(CurationEntity instance) {
		for(CurationStep step: pipeline)
			if(!step.apply(instance))
				return; //stop applying further processors in case of fatal curation error
		
		instance.setValid(true);
	}
	
	protected abstract Collection<CurationStep> initPipeline();
	
	

}