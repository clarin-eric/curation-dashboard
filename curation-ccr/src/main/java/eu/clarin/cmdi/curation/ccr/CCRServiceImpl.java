package eu.clarin.cmdi.curation.ccr;

import eu.clarin.cmdi.curation.ccr.cache.CCRCache;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import org.springframework.stereotype.Service;


/**
 * The type Ccr service.
 */
@Service
class CCRServiceImpl implements CCRService {

   private final CCRCache cache;

   public CCRServiceImpl(CCRCache cache) {
      this.cache = cache;
   }

   /**
    * Gets concept.
    *
    * @param uri the uri of the ccr concept
    * @return the concept
    */
   @Override
   public CCRConcept getConcept(String uri) throws CCRServiceNotAvailableException {

      return cache.getCCRConcept(uri);
   }
}
