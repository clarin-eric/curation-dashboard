package eu.clarin.cmdi.curation.cr.profile_parser;

import eu.clarin.cmdi.curation.ccr.CCRService;

public class ProfileParserFactory {
	
	
	public static ProfileParser createParser(String cmdVersion, CCRService ccrService){
		switch(cmdVersion){
			case "1.1": 
				return new CMDI1_1_ProfileParser(ccrService);
			case "1.x":
			case "1.2":
				return new CMDI1_2_ProfileParser(ccrService);				
			default:
				throw new UnsupportedOperationException("Parser for the specified CMDI version: " + cmdVersion + " doesnt exist!");
		}		
	}

}
