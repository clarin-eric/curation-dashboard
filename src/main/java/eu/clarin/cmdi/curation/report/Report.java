package eu.clarin.cmdi.curation.report;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;


/**
 * @author dostojic
 * 
 * A report about CMDI files, for details check https://trac.clarin.eu/wiki/Cmdi/QualityCriteria
 *
 */
 public class Report {
	
	private List<Message> messages;
	private boolean verbose;
	private boolean debug;
	private String name;
	
	public Report(String name, boolean verbose, boolean debug){
		this.name = name;
		this.verbose = verbose;
		//this.debug = debug;
		this.debug = true;
		messages = new LinkedList<>();
	}
	
	public Report(String name, boolean verbose){
		this(name, verbose, false);
	}
	
	public Report(String name){
		this(name, true, false);
	}
	
	public void addMessage(@Nonnull Message m){
		messages.add(m);
	}
	
	public void addMessage(@Nonnull String m){
		messages.add(new Message(m));
	}
	
	public void addDebugMessage(@Nonnull String m){
		if(debug)
			messages.add(new Message(Severity.DEBUG, m));
	}
	
	
	public Severity getHighestSeverity(){
		//if list is empty we run info, we care only about fatals		
		return messages.isEmpty()? Severity.INFO : messages
													.stream()
													.max(Message::compareTo)
													.map(Message::getSeverity)
													.get();
	}
	
	public Message getFirst(){
		return messages == null || messages.isEmpty() ? null : messages.iterator().next();
	}
	
	/*
	 * Take this value with caution, it is very speculative.
	 * We consider validation/evaluation/assessment as successful (passed) if the highest severity in messages is INFO or WARNING
	 * but how we decide about severity is another story 
	 */
	
	public boolean isPassed(){
		return getHighestSeverity().compareTo(Severity.WARNING) <= 0;
	}
	
	
	public String toString(){
		//Collections.sort(messages);
		StringBuilder sb = new StringBuilder(2000);
		sb.append(name).append("\n").append("STATUS: ").append(isPassed()? "OK" : "FAILED").append("\n");
		messages
			.stream()
			.filter(Message::isInfo)
			.forEach(msg -> sb.append("\t").append(msg).append("\n"));
		if(!isPassed() || verbose)
			messages
			.stream()
			.filter(Message::isNonInfo)
			.forEach(msg -> sb.append("\t").append(msg).append("\n"));	
		if(debug){
			messages
			.stream()
			.filter(Message::isDebug)
			.forEach(msg -> sb.append("\t").append(msg).append("\n"));
		}
		return sb.toString();
	}
	
	
	public void setDebug(boolean debug){
		this.debug = debug;
	}
}
