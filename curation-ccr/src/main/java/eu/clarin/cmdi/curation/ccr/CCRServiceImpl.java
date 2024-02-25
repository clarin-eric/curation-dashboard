package eu.clarin.cmdi.curation.ccr;

import java.util.Collection;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.clarin.cmdi.curation.ccr.cache.CCRCache;


/**
 * The type Ccr service.
 */
@Service
class CCRServiceImpl implements CCRService {

   @Autowired
   private CCRCache cache;

   /**
    * Gets concept.
    *
    * @param uri the uri of the ccr concept
    * @return the concept
    */
   @Override
   public synchronized CCRConcept getConcept(String uri) throws CCRServiceNotAvailableException {

      return cache.getCCRConcept(uri);
   }
}
