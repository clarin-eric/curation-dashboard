/**
 * 
 */
package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.subprocessor.ProcessingActivity;
import eu.clarin.cmdi.curation.subprocessor.ProfileFacetCoverage;

/**
 * @author dostojic
 *
 */
public class CMDIProfileProcessor extends AbstractProcessor{

	
	@Override
	protected Collection<ProcessingActivity> createPipeline() {
		return Arrays.asList(
				new ProfileFacetCoverage()
				);
	}

}
