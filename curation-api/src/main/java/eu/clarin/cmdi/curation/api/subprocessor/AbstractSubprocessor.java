/**
 * 
 */
package eu.clarin.cmdi.curation.api.subprocessor;

import eu.clarin.cmdi.curation.api.entities.CMDInstance;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;


public abstract class AbstractSubprocessor extends AbstractMessageCollection{

    public abstract void process(CMDInstance entity, CMDInstanceReport report) throws Exception;

}
