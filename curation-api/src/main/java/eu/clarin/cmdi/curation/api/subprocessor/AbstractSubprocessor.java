/**
 * 
 */
package eu.clarin.cmdi.curation.api.subprocessor;

import eu.clarin.cmdi.curation.api.entities.CMDInstance;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.Score;


public abstract class AbstractSubprocessor extends AbstractMessageCollection{

    public abstract void process(CMDInstance entity, CMDInstanceReport report) throws Exception;
    
    public abstract Score calculateScore(CMDInstanceReport report);

}
