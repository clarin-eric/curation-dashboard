package eu.clarin.cmdi.curation.report;

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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(severity.label + " -")
				.append(line != 0 ? "line:" + line + " " : " ")
				.append(col != 0 ?  "column:" + col + " " : " ")
				.append(message + " ")
				.append(cause != null ? cause.getMessage() : "")
				.toString();			   
	}


	@Override
	public int compareTo(Message msg) {		
		//compare by severity then line and finally by column
		
		return sign(severity.priority, msg.severity.priority) == 0 ? 0 : 
			   sign(line, msg.line) 						  == 0 ? 0 :
			   sign(col, msg.col);

	}
	
	private int sign(int a, int b){
		return a > b ? +1 : a < b ? -1 : 0;
	}

}
