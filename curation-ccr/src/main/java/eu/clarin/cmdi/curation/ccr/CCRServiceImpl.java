package eu.clarin.cmdi.curation.ccr;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.clarin.cmdi.curation.ccr.cache.CCRCache;


@Service
class CCRServiceImpl implements CCRService {

   @Autowired
   private CCRCache cache;

   @Override
   public CCRConcept getConcept(String url) {
      return cache.getCCRConceptMap().get(url);
   }

   @Override
   public Collection<CCRConcept> getAll() {
      return cache.getCCRConceptMap().values();
   }

}
