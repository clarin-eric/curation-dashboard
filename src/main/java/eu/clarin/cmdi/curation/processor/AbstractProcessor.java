package eu.clarin.cmdi.curation.processor;

import java.util.Collection;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.subprocessor.ProcessingActivity;

public abstract class AbstractProcessor<R extends Report>{

    public R process(CurationEntity entity) {
	R report = createReport();
	for (ProcessingActivity activity : createPipeline())
	    // stop applying further processors in case of fatal error
	    if (!activity.process(entity, report))
		break;
	return report;
    }

    protected abstract Collection<ProcessingActivity> createPipeline();
    
    protected abstract R createReport();

}