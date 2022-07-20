package eu.clarin.cmdi.curation.ccr;

import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.ccr.config.CCRProperties;

@Slf4j
@Component
public class CCRServiceFactory {

   @Autowired
   CCRProperties ccrProps;

   private CCRService crrService;

   public CCRServiceFactory(){

	}
   @PostConstruct
   public void init() {
      try {
         // enableTimer is only set when ccrservice is called from the webmodule and not
         // the coremodule
         // because coremodule doesnt terminate when there is a timer
         if (ccrProps.isEnableTimer()) {
            
            log.info("timer enabled for daily instantiation of CRRService");

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

               @Override
               public void run() {
                  synchronized (crrService) {

                     crrService = new CCRService(ccrProps);

                  }

               }
            }, 60 * 60 * 1000 /* Once per day */);

         }
         else {
            crrService = new CCRService(ccrProps);
         }

      }
      catch (Exception e) {
         log.error("", e);
      }      
   }
   

   public ICCRService getCCRService() {



      return this.crrService;

   }
}
