package eu.clarin.cmdi.curation.processor;

import java.util.Collection;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;

public abstract class AbstractProcessor<R extends Report> {

	public Report process(CurationEntity entity) {
		Report report = createReport();
		for (ProcessingStep step : createPipeline())
			// stop applying further processors in case of fatal error
			if (!step.process(entity, report))
				break;
		report.calculateScore();
		return report;
	}

	protected abstract Collection<ProcessingStep> createPipeline();

	protected abstract R createReport();

}