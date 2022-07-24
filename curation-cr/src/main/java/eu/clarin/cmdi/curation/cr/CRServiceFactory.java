package eu.clarin.cmdi.curation.cr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class CRServiceFactory {
   
   @Autowired
   CRService crService;

   public CRService getCRService() {
      
      return crService;
   }
}
