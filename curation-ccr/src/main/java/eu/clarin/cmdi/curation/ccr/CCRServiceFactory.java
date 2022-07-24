package eu.clarin.cmdi.curation.ccr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CCRServiceFactory {
   
   @Autowired
   CCRService service;
   
   public CCRService getCCRService() {

      return service;

   }
}
