package eu.clarin.cmdi.curation.ccr.cache;

import eu.clarin.cmdi.curation.ccr.CCRConcept;
import eu.clarin.cmdi.curation.ccr.CCRService;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
class CCRServiceImpl implements CCRService {

   @Autowired
   private CCRConceptMapCache cache;

   @Override
   public CCRConcept getConcept(String url) {
      return cache.getCCRConceptMap().get(url);
   }

   @Override
   public Collection<CCRConcept> getAll() {
      return cache.getCCRConceptMap().values();
   }

}
