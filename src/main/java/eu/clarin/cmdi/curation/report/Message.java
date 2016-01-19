package eu.clarin.cmdi.curation.report;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Message implements Comparable<Message>{
	
	private final Severity severity;
    private final int line;
    private final int col;
    private final String message;
    private final Throwable cause;
	
    
    public Message(Severity severity, int line, int col, String message, Throwable cause) {
		super();
		this.severity = severity;
		this.line = line;
		this.col = col;
		this.message = message;
		this.cause = cause;
	}
    
    public Message(Severity severity, int line, int col, String message) {
		this(severity, line, col, message, null);
	}
    
    public Message(Severity severity, String message){
    	this(severity, 0, 0, message, null);
    }
    
    public Message(Severity severity, String message, Exception e){
    	this(severity, 0, 0, message, e);
    }
    
    public Message(String message){
    	this(Severity.INFO, 0, 0, message, null);
    }
    
    
	public Severity getSeverity() {
		return severity;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getCol() {
		return col;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Throwable getCause() {
		return cause;
	}
	
	public boolean isFatal(){
		return severity == Severity.FATAL;
	}
	
	public boolean isInfo(){
		return severity == Severity.INFO;
	}
	
	public boolean isDebug(){
		return severity == Severity.DEBUG;
	}
	
	public boolean isNonInfo(){
		return severity != Severity.INFO && severity != Severity.DEBUG;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append(severity.label + " -")
				.append(line != 0 ? ("line:" + line + " ") : " ")
				.append(col != 0 ?  ("column:" + col + " ") : " ")
				.append(message + " ")
				.append(cause != null ? cause.getMessage() : "")
				.toString();			   
	}


	@Override
	public int compareTo(Message msg) {		
		//compare by severity then line and finally by column
		return Integer.compare(severity.priority, msg.severity.priority) != 0? Integer.compare(severity.priority, msg.severity.priority) :
			   Integer.compare(line, msg.line) != 0? Integer.compare(line, msg.line) :
			   Integer.compare(col, msg.col);
	}

}
