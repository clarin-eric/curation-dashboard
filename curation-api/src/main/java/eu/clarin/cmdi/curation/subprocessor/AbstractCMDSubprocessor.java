/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;


public abstract class AbstractCMDSubprocessor extends AbstractMessageCollection{

    public abstract void process(CMDInstance entity, CMDInstanceReport report) throws Exception;

}
