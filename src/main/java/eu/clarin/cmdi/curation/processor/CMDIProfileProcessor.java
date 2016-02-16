/**
 * 
 */
package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.subprocessor.ProcessingActivity;

/**
 * @author dostojic
 *
 */
public class CMDIProfileProcessor extends AbstractProcessor {

    @Override
    protected Collection<ProcessingActivity> createPipeline() {
	return Arrays.asList(
		//new ProfileFacetCoverage()
		);
    }


    @Override
    protected Report createReport() {
	throw new UnsupportedOperationException();
    }


}
