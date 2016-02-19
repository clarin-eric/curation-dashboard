package eu.clarin.cmdi.curation.main;

public class Config {
	
	
	public static final String SCORE_NUMERIC_DISPLAY_FORMAT = "#0.000";
	
	//VLO restriction
	public static final long MAX_SIZE_OF_FILE = 30 * 1024; //30 KB
	
	//huge performance impact, use it only for smaller collections <100
	public static final boolean HTTP_VALIDATION = false;
	
	public static final boolean GENERATE_CHILDREN_REPORTS = false;

	public static final String OUTPUT_DIRECTORY = "";
}
