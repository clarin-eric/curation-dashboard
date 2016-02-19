/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;

/**
 * @author dostojic
 *
 */
public abstract class CMDISubprocessor implements ProcessingStep<CMDIInstance, CMDIInstanceReport> {

    @Override
    public abstract boolean process(CMDIInstance entity, CMDIInstanceReport report);

}
