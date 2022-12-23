package eu.clarin.cmdi.curation.api.subprocessor.instance;

import eu.clarin.linkchecker.persistence.model.Client;
import eu.clarin.linkchecker.persistence.model.Status;
import eu.clarin.linkchecker.persistence.model.Url;
import eu.clarin.linkchecker.persistence.repository.ClientRepository;
import eu.clarin.linkchecker.persistence.repository.StatusRepository;
import eu.clarin.linkchecker.persistence.repository.UrlRepository;
import eu.clarin.linkchecker.persistence.service.LinkService;
import eu.clarin.linkchecker.persistence.utils.Category;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.report.Severity;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import eu.clarin.cmdi.vlo.PIDUtils;

@Slf4j
@Component
public class URLValidator extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   private ApiConfig conf;
   @Autowired
   private LinkService uService;
   @Autowired
   private UrlRepository uRepository;
   @Autowired
   private StatusRepository sRepository;
   @Autowired
   private ClientRepository clRepository;
   

   @Override
   public void process(CMDInstance instance, CMDInstanceReport instanceReport) {
      
      Score score = new Score("url-validation", 1.0);
      
      instanceReport.urlReport = new CMDInstanceReport.URLReport();

      CMDIData<Map<String, List<ValueSet>>> data = instance.getCmdiData();

      
      Stream<Resource> resourceStream = Stream.of(
            data.getDataResources().stream(),
            data.getLandingPageResources().stream(),
            data.getMetadataResources().stream(),
            data.getSearchPageResources().stream(),
            data.getSearchResources().stream()
         ).flatMap(s -> s);           


      if ("collection".equalsIgnoreCase(conf.getMode()) || "all".equalsIgnoreCase(conf.getMode())) {
         
         Client client = this.clRepository.findByName(conf.getClientUsername()).get();
         
         
         final String origin = conf.getDirectory().getDataRoot().relativize(instance.getPath()).toString();
         
         
         resourceStream.forEach(resource -> {
            
            instanceReport.urlReport.numOfLinks++;
            
            if(PIDUtils.isPid(resource.getResourceName())) {
            
               uService.save(
                     client, 
                     PIDUtils.getActionableLinkForPid(resource.getResourceName()), 
                     origin, 
                     instance.getProvidergroupName(), 
                     (resource.getMimeType()==null?"Not Specified":resource.getMimeType())
                  );
            }
         });
         
         if(data.getDocument().get("_selfLink") != null && !data.getDocument().get("_selfLink").isEmpty() && PIDUtils.isPid(data.getDocument().get("_selfLink").get(0).getValue())) {
            uService.save(
                  client, 
                  PIDUtils.getActionableLinkForPid(data.getDocument().get("_selfLink").get(0).getValue()), 
                  origin, 
                  instance.getProvidergroupName(), 
                  "Not Specified"
               );
         }

      } // end collection mode
      else {// instance mode
         resourceStream.forEach(resource -> {
            
            Optional<Url> urlOptional = null;
            Optional<Status> statusOptional = null;
            
            if(!PIDUtils.isPid(resource.getResourceName())) {
               score.addMessage(Severity.ERROR, "Url: " + resource.getResourceName() + " Category:" + Category.Invalid_URL);
               instanceReport.urlReport.numOfInvalidLinks++;
            }
            else if((urlOptional = uRepository.findByName(PIDUtils.getActionableLinkForPid(resource.getResourceName()))).isPresent()
                  && (statusOptional = sRepository.findByUrl(urlOptional.get())).isPresent()) {
               
               instanceReport.urlReport.numOfCheckedLinks++;
               
               long numOfValidLinks = 0;

               switch (statusOptional.get().getCategory()) {
                  case Ok -> numOfValidLinks++;               
                  case Broken -> {
                     score.addMessage(Severity.ERROR, "Url: " + urlOptional.get().getName() + " Category:" + statusOptional.get().getCategory());
                     instanceReport.urlReport.numOfBrokenLinks++;
                  }
                  case Invalid_URL -> {
                     score.addMessage(Severity.ERROR, "Url: " + urlOptional.get().getName() + " Category:" + statusOptional.get().getCategory());
                     instanceReport.urlReport.numOfInvalidLinks++;
                  }
                  case Undetermined -> {
                     score.addMessage(Severity.WARNING,
                           "Url: " + urlOptional.get().getName() + " Category:" + statusOptional.get().getCategory());
                     numOfValidLinks++;
                     instanceReport.urlReport.numOfUndeterminedLinks++;
                  }
                  case Restricted_Access -> {
                     score.addMessage(Severity.WARNING,
                           "Url: " + urlOptional.get().getName() + " Category:" + statusOptional.get().getCategory());
                     numOfValidLinks++;
                     instanceReport.urlReport.numOfRestrictedAccessLinks++;
                  }
                  case Blocked_By_Robots_txt -> {
                     score.addMessage(Severity.WARNING,
                           "Url: " + urlOptional.get().getName() + " Category:" + statusOptional.get().getCategory());
                     numOfValidLinks++;
                     instanceReport.urlReport.numOfBlockedByRobotsTxtLinks++;
                  }
               }

               CMDInstanceReport.URLElement urlElementReport = new CMDInstanceReport.URLElement()
                     .convertFromLinkCheckerURLElement(statusOptional.get());
               instanceReport.addURLElement(urlElementReport);
            
               instanceReport.urlReport.percOfValidLinks = instanceReport.urlReport.numOfCheckedLinks == 0 ? 0
                     : (numOfValidLinks / (double) instanceReport.urlReport.numOfCheckedLinks);            
            }
         });
      } // end instance mode
      
      score.setScore(instanceReport.urlReport.percOfValidLinks);
      
      instanceReport.addSegmentScore(score);

   }
}
