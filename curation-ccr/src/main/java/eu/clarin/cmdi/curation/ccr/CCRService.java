package eu.clarin.cmdi.curation.ccr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;


public interface CCRService {
    /**
     * Returns a CCR concepts for a given uri
     * or null if concept doesn't exist
     *
     * @param uri the uri of a concept
     * @throws CCRServiceNotAvailableException
     * @return    single concept or null
     * @see CCRConcept
     */
    public CCRConcept getConcept(String uri) throws CCRServiceNotAvailableException;
}
