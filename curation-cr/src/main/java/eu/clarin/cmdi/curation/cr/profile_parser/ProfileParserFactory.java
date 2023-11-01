package eu.clarin.cmdi.curation.cr.profile_parser;

import eu.clarin.cmdi.curation.ccr.CCRService;

/**
 * The type Profile parser factory.
 */
public class ProfileParserFactory {


	/**
	 * Create parser profile parser.
	 *
	 * @param cmdVersion the cmd version
	 * @param ccrService the ccr service
	 * @return the profile parser
	 */
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
