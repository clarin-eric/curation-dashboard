package eu.clarin.cmdi.curation.report;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;


/**
 * @author dostojic
 * 
 * A report about CMDI files, for details check https://trac.clarin.eu/wiki/Cmdi/QualityCriteria
 *
 */
 public class CMDIReport {	
	
	private Collection<Message> messages;	
	
	public void addMessage(@Nonnull Message m){
		//init collection in a lazy way
		if(messages == null)
			messages = new TreeSet<Message>();
		
		messages.add(m);
	}
	
	
	public Severity getHighestSeverity(){
		Message msg = getFirst();
		return msg == null? null : msg.getSeverity();
		
	}
	
	public Message getFirst(){
		return messages == null || messages.isEmpty() ? null : messages.iterator().next();
	}
	
	
}
