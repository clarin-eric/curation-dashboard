/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;

/**
 * @author dostojic
 *
 */
public abstract class CMDSubprocessor extends ProcessingStep<CMDInstance, CMDInstanceReport> {

    @Override
    public abstract void process(CMDInstance entity, CMDInstanceReport report) throws Exception;

}
