package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.subprocessor.CurationTask;
import eu.clarin.cmdi.curation.subprocessor.Dummy;

/**
 * @author dostojic
 *
 */
public class DirectoryProcessor extends AbstractProcessor {

	@Override
	protected Collection<CurationTask> createPipeline() {
		return Arrays.asList(
				new Dummy()
				);
	}


}
