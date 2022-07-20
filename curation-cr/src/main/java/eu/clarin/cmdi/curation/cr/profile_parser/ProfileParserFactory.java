package eu.clarin.cmdi.curation.cr.profile_parser;

public class ProfileParserFactory {
	
	
	public static ProfileParser createParser(String cmdVersion){
		switch(cmdVersion){
			case "1.1": 
				return new CMDI1_1_ProfileParser();
			case "1.x":
			case "1.2":
				return new CMDI1_2_ProfileParser();				
			default:
				throw new UnsupportedOperationException("Parser for the specified CMDI version: " + cmdVersion + " doesnt exist!");
		}		
	}

}
