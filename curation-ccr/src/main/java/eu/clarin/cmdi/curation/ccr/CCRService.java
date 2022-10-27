package eu.clarin.cmdi.curation.ccr;

import java.util.Collection;


public interface CCRService {
	
	
	/**
	 * Returns all existing concepts in CCR prefetched and cached during initialization
	 *
	 * 
	 * @return	all concepts from the CCR
	 * @throws CCRServiceNotAvailableException 
	 * @see CCRConcept
	 */
	public Collection<CCRConcept> getAll();
	
	
	/**
	 * Returns a CCR concepts for a given uri
	 * or null if concept doesn't exist
	 *
	 * 
	 * @param uri The URI of a concept
	 * @return	single concept or null
	 * @throws CCRServiceNotAvailableException 
	 * @see CCRConcept
	 */
	public CCRConcept getConcept(String uri);
}
