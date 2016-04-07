/**
 * 
 */
package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import eu.clarin.cmdi.curation.subprocessor.ProfileComponentsHandler;
import eu.clarin.cmdi.curation.subprocessor.ProfileConceptsHandler;
import eu.clarin.cmdi.curation.subprocessor.ProfileFacetHanlder;
import eu.clarin.cmdi.curation.subprocessor.ProfileHeaderHandler;

/**
 * @author dostojic
 *
 */
public class CMDProfileProcessor extends AbstractProcessor<CMDProfileReport> {

    /* (non-Javadoc)
     * @see eu.clarin.cmdi.curation.processor.AbstractProcessor#createPipeline()
     */
    @Override
    protected Collection<ProcessingStep> createPipeline() {
	return Arrays.asList(
		new ProfileHeaderHandler(),
		new ProfileComponentsHandler(),
		new ProfileConceptsHandler(),
		new ProfileFacetHanlder()
		);
    }

    /* (non-Javadoc)
     * @see eu.clarin.cmdi.curation.processor.AbstractProcessor#createReport()
     */
    @Override
    protected CMDProfileReport createReport() {
	return new CMDProfileReport();
    }





}
