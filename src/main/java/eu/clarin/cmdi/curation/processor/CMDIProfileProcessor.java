/**
 * 
 */
package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.report.CMDIProfileReport;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import eu.clarin.cmdi.curation.subprocessor.ProfileComponentsHandler;
import eu.clarin.cmdi.curation.subprocessor.ProfileElementsHandler;
import eu.clarin.cmdi.curation.subprocessor.ProfileFacetHanlder;

/**
 * @author dostojic
 *
 */
public class CMDIProfileProcessor extends AbstractProcessor<CMDIProfileReport> {

    /* (non-Javadoc)
     * @see eu.clarin.cmdi.curation.processor.AbstractProcessor#createPipeline()
     */
    @Override
    protected Collection<ProcessingStep> createPipeline() {
	return Arrays.asList(
		new ProfileComponentsHandler(),
		new ProfileElementsHandler(),
		new ProfileFacetHanlder()
		);
    }

    /* (non-Javadoc)
     * @see eu.clarin.cmdi.curation.processor.AbstractProcessor#createReport()
     */
    @Override
    protected CMDIProfileReport createReport() {
	return new CMDIProfileReport();
    }





}
