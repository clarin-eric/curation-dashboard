package eu.clarin.cmdi.curation.ccr;

import lombok.Data;

/**
 * The type Ccr concept.
 */
@Data
public class CCRConcept{

	private final String uri;
	
	private final String prefLabel;
	
	private final CCRStatus status;

}
