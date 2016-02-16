package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.subprocessor.ProcessingActivity;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.subprocessor.CollectionAggregator;

/**
 * @author dostojic
 *
 */
public class DirectoryProcessor extends AbstractProcessor<CollectionReport> {

	@Override
	protected Collection<ProcessingActivity> createPipeline() {
		return Arrays.asList(
				new CollectionAggregator()
				);
	}

	@Override
	protected CollectionReport createReport() {
	   return new CollectionReport();
	}


}
