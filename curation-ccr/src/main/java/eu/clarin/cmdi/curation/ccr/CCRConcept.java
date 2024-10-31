package eu.clarin.cmdi.curation.ccr;

import lombok.Data;

import java.io.Serializable;

/**
 * The type Ccr concept.
 */
@Data
public class CCRConcept implements Serializable {

	private final String uri;
	
	private final String prefLabel;
	
	private final CCRStatus status;

}
