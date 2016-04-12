package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.List;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 * 
 */


public abstract class ProcessingStep<T extends CurationEntity, R extends Report> {

    protected List<Message> msgs = null;

    /**
     * @param entity 
     * @param report
     * @return true if processing finished without fatal errors
     */
    public abstract boolean process(T entity, R report);
    
    
    protected void addMessage(Severity lvl, String message){
	if(msgs == null){
	    msgs = new ArrayList<>();
	}
	msgs.add(new Message(lvl, message));
    }
}