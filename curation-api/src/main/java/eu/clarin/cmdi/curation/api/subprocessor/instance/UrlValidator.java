package eu.clarin.cmdi.curation.api.subprocessor.instance;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.PIDUtils;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import eu.clarin.linkchecker.persistence.model.Client;
import eu.clarin.linkchecker.persistence.repository.ClientRepository;
import eu.clarin.linkchecker.persistence.service.LinkService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

/**
 * The type Url validator.
 */
@Slf4j
@Component
public class UrlValidator extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   private ApiConfig conf;
   @Autowired
   private LinkService lService;
   @Autowired
   private ClientRepository clRepository;
   // restricting parallel database access - the default size of the hikari cp is 10
   private static final Semaphore semaphore = new Semaphore(10);
   
   private Client client;
   
   @PostConstruct
   private void setClient() {
      if(conf.getClientUsername() == null || conf.getClientUsername().isEmpty()) {
         log.error("the poperty curation.clientUsername must be set");
         throw new RuntimeException();
      }
      this.clRepository
         .findByName(conf.getClientUsername()).ifPresentOrElse(cl -> this.client=cl, () -> {
            log.error("there is no client with name '{}' in the client table", conf.getClientUsername());
            throw new RuntimeException("make sure property 'curation.clientUsername' is set and client with this name is in database");
         });
   }


   /**
    * Process.
    *
    * @param instance       the instance
    * @param instanceReport the instance report
    */
   @Override
   public void process(CMDInstance instance, CMDInstanceReport instanceReport) {
      
      if(instance.getCmdiData().isEmpty()) {
         
         log.debug("can't create CMDData object from file '{}'", instance.getPath());
         instanceReport.details
               .add(new Detail(Severity.FATAL, "file", "can't parse file '" + instance.getPath().getFileName() + "'"));
         instanceReport.isProcessable = false;
         
         return;
      }

      if ("collection".equalsIgnoreCase(conf.getMode()) || "all".equalsIgnoreCase(conf.getMode())) {
         
         CMDIData<Map<String, List<ValueSet>>> data = instance.getCmdiData().get();
         


         
         Stream<Resource> resourceStream = Stream.of(
               data.getDataResources().stream(),
               data.getLandingPageResources().stream(),
               data.getMetadataResources().stream(),
               data.getSearchPageResources().stream(),
               data.getSearchResources().stream()
            ).flatMap(s -> s); 
         

         
         
         final String origin = conf.getDirectory().getDataRoot().relativize(instance.getPath()).toString();
         
         final Collection<Pair<String,String>> urlMimes = new ArrayList<Pair<String,String>>();
         
         resourceStream.forEach(resource -> {
            
            if(PIDUtils.isActionableLink(resource.getResourceName()) || PIDUtils.isPid(resource.getResourceName())) {
            
               urlMimes.add(
                  Pair.of(
                     PIDUtils.getActionableLinkForPid(resource.getResourceName()), 
                     resource.getMimeType()==null?"Not Specified":resource.getMimeType()
                  )
               );
            }
         });
         
         
         if(data.getDocument().get("_selfLink") != null && !data.getDocument().get("_selfLink").isEmpty() && PIDUtils.isActionableLink(data.getDocument().get("_selfLink").get(0).getValue())) {
            urlMimes.add(
               Pair.of( 
                  PIDUtils.getActionableLinkForPid(data.getDocument().get("_selfLink").get(0).getValue()), 
                  "Not Specified"
               )
            );
         }

         try{

            UrlValidator.semaphore.acquire();
            lService.savePerOrigin(client, instance.getProvidergroupName(), origin, urlMimes);
         }
         catch (InterruptedException e) {

             throw new RuntimeException(e);
         }
         finally {

            UrlValidator.semaphore.release();
         }
      }
   }
}
