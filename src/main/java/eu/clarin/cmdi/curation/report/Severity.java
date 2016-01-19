package eu.clarin.cmdi.curation.report;

public enum Severity {
    
	DEBUG(0, "DEBUG"),
	INFO(1, "INFO"), 
	WARNING(2, "WARNING"),
	ERROR(3, "ERROR"),
	FATAL(4, "FATAL");
	
    
    final int priority;
    final String label;
    
	private Severity(int priority, String label) {
		this.priority = priority;
		this.label = label;
	}


	public int getPriority() {
		return priority;
	}
}
