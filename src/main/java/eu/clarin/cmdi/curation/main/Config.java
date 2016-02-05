package eu.clarin.cmdi.curation.main;

import eu.clarin.cmdi.curation.report.Severity;

public class Config {
	
	
	public static final String SCORE_NUMERIC_DISPLAY_FORMAT = "#0.000";
	
	public static final long MAX_SIZE_OF_FILE = 30 * 1024; //30 KB
	
	public static String schematronFile = "default.sch";
	
	public static boolean INCLUDE_DETAILS = false;
	
	public static Severity REPORT_VERBOSITY = Severity.INFO;
	
	public static boolean PRINT_COLLECTION_DETAILS = true;
	
}
