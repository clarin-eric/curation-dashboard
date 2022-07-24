package eu.clarin.cmdi.curation.ccr;

import lombok.Data;

@Data
public class CCRConcept{

	private final String uri;
	
	private final String prefLabel;
	
	private final CCRStatus status;

}
