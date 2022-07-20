package eu.clarin.cmdi.curation.ccr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CCRConcept{

	private String uri;
	
	private String prefLabel;
	
	private CCRStatus status;

}
