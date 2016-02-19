package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.subprocessor.CollectionAggregator;

/**
 * @author dostojic
 *
 */
public class CollectionProcessor extends AbstractProcessor<CollectionReport> {

	@Override
	protected Collection<ProcessingStep> createPipeline() {
		return Arrays.asList(
				new CollectionAggregator()
				);
	}

	@Override
	protected CollectionReport createReport() {
	   return new CollectionReport();
	}


}
