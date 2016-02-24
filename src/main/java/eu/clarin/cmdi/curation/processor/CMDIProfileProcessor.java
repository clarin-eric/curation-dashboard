/**
 * 
 */
package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.report.CMDIProfileReport;
import eu.clarin.cmdi.curation.subprocessor.CMDIProfileFacets;
import eu.clarin.cmdi.curation.subprocessor.CMDIProfileValidator;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;

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
		new CMDIProfileValidator(),
		new CMDIProfileFacets()
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
