package eu.clarin.cmdi.curation.api.subprocessor.instance;

import eu.clarin.linkchecker.persistence.model.Client;
import eu.clarin.linkchecker.persistence.repository.ClientRepository;
import eu.clarin.linkchecker.persistence.service.LinkService;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import eu.clarin.cmdi.vlo.PIDUtils;

@Slf4j
@Component
public class UrlValidator extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   private ApiConfig conf;
   @Autowired
   private LinkService uService;
   @Autowired
   private ClientRepository clRepository;
   
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
   

   @Override
   public void process(CMDInstance instance, CMDInstanceReport instanceReport) {

      if ("collection".equalsIgnoreCase(conf.getMode()) || "all".equalsIgnoreCase(conf.getMode())) {
         
         CMDIData<Map<String, List<ValueSet>>> data = instance.getCmdiData();

         
         Stream<Resource> resourceStream = Stream.of(
               data.getDataResources().stream(),
               data.getLandingPageResources().stream(),
               data.getMetadataResources().stream(),
               data.getSearchPageResources().stream(),
               data.getSearchResources().stream()
            ).flatMap(s -> s); 
         

         
         
         final String origin = conf.getDirectory().getDataRoot().relativize(instance.getPath()).toString();
         
         
         resourceStream.forEach(resource -> {
            
            if(PIDUtils.isActionableLink(resource.getResourceName()) || PIDUtils.isPid(resource.getResourceName())) {
            
               uService.save(
                     client, 
                     PIDUtils.getActionableLinkForPid(resource.getResourceName()), 
                     origin, 
                     instance.getProvidergroupName(), 
                     (resource.getMimeType()==null?"Not Specified":resource.getMimeType())
                  );
            }
         });
         
         if(data.getDocument().get("_selfLink") != null && !data.getDocument().get("_selfLink").isEmpty()) {
            uService.save(
                  client, 
                  PIDUtils.getActionableLinkForPid(data.getDocument().get("_selfLink").get(0).getValue()), 
                  origin, 
                  instance.getProvidergroupName(), 
                  "Not Specified"
               );
         }
      }
   }
}
