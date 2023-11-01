package eu.clarin.cmdi.curation.ccr;

import java.util.Collection;

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
   public CCRConcept getConcept(String uri) {
      return cache.getCCRConceptMap().get(uri);
   }

   /**
    * Gets all.
    *
    * @return the all
    */
   @Override
   public Collection<CCRConcept> getAll() {
      return cache.getCCRConceptMap().values();
   }

}
