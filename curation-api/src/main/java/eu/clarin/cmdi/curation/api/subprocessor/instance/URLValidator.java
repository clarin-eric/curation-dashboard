package eu.clarin.cmdi.curation.api.subprocessor.instance;

import eu.clarin.cmdi.cpa.model.Client;
import eu.clarin.cmdi.cpa.model.Status;
import eu.clarin.cmdi.cpa.model.Url;
import eu.clarin.cmdi.cpa.repository.ClientRepository;
import eu.clarin.cmdi.cpa.repository.StatusRepository;
import eu.clarin.cmdi.cpa.repository.UrlRepository;
import eu.clarin.cmdi.cpa.service.LinkService;
import eu.clarin.cmdi.cpa.utils.Category;
import eu.clarin.cmdi.cpa.utils.UrlValidator;
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

@Slf4j
@Component
public class URLValidator extends AbstractSubprocessor {

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
   public synchronized void process(CMDInstance entity, CMDInstanceReport instanceReport) {
      
      instanceReport.urlReport = new CMDInstanceReport.URLReport();

      CMDIData<Map<String, List<ValueSet>>> data = entity.getCmdiData();

      Map<String, Resource> urlMap = new HashMap<>();

      ArrayList<Resource> resources = new ArrayList<>();
      resources.addAll(data.getDataResources());
      resources.addAll(data.getLandingPageResources());
      resources.addAll(data.getMetadataResources());
      resources.addAll(data.getSearchPageResources());
      resources.addAll(data.getSearchResources());

      resources.stream().filter(resource -> resource.getResourceName() != null
            && (resource.getResourceName().startsWith("http://") || resource.getResourceName().startsWith("https://")))
            .forEach(resource -> {
               urlMap.computeIfAbsent(resource.getResourceName(), key -> resource);
               instanceReport.urlReport.numOfLinks++;
            });

      String selfLink = (data.getDocument().get("_selfLink") != null && !data.getDocument().get("_selfLink").isEmpty())
            ? data.getDocument().get("_selfLink").get(0).getValue()
            : "";

      // only add selfLink if url
      if (selfLink.startsWith("http://") || selfLink.startsWith("https://"))
         urlMap.computeIfAbsent(selfLink, key -> null);


      if ("collection".equals(conf.getMode())) {
         
         Client client = this.clRepository.findByName(conf.getClientUsername()).get();

         for (String url : urlMap.keySet()) {

            String origin = conf.getDirectory().getDataRoot().relativize(entity.getPath()).toString();

            String providergroup = instanceReport.parentName;

            String expectedMimeType = urlMap.get(url).getMimeType();
            expectedMimeType = expectedMimeType == null ? "Not Specified" : expectedMimeType;

            uService.save(client, url, origin, providergroup, expectedMimeType);
         }
      } // end collection mode
      else {// instance mode
         for (String url : urlMap.keySet()) {
            
            Optional<Url> urlOptional = null;
            Optional<Status> statusOptional = null;
            
            if(!UrlValidator.validate(url).isValid()) {
               addMessage(Severity.ERROR, "Url: " + url + " Category:" + Category.Invalid_URL);
               instanceReport.urlReport.numOfInvalidLinks++;
            }
            else if((urlOptional = uRepository.findByName(url)).isPresent()
                  && (statusOptional = sRepository.findByUrl(urlOptional.get())).isPresent()) {
               
               instanceReport.urlReport.numOfCheckedLinks++;
               
               long numOfValidLinks = 0;

               switch (statusOptional.get().getCategory()) {
                  case Ok -> numOfValidLinks++;               
                  case Broken -> {
                     addMessage(Severity.ERROR, "Url: " + urlOptional.get().getName() + " Category:" + statusOptional.get().getCategory());
                     instanceReport.urlReport.numOfBrokenLinks++;
                  }
                  case Invalid_URL -> {
                     addMessage(Severity.ERROR, "Url: " + urlOptional.get().getName() + " Category:" + statusOptional.get().getCategory());
                     instanceReport.urlReport.numOfInvalidLinks++;
                  }
                  case Undetermined -> {
                     addMessage(Severity.WARNING,
                           "Url: " + urlOptional.get().getName() + " Category:" + statusOptional.get().getCategory());
                     numOfValidLinks++;
                     instanceReport.urlReport.numOfUndeterminedLinks++;
                  }
                  case Restricted_Access -> {
                     addMessage(Severity.WARNING,
                           "Url: " + urlOptional.get().getName() + " Category:" + statusOptional.get().getCategory());
                     numOfValidLinks++;
                     instanceReport.urlReport.numOfRestrictedAccessLinks++;
                  }
                  case Blocked_By_Robots_txt -> {
                     addMessage(Severity.WARNING,
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
         }
      } // end instance mode

   }

   public synchronized Score calculateScore(CMDInstanceReport report) {
      // it can influence the score, if one collection was done with enabled and the
      // other without

      double score = report.urlReport.percOfValidLinks != null && !Double.isNaN(report.urlReport.percOfValidLinks)
            ? report.urlReport.percOfValidLinks
            : 0;
      return new Score(score, 1.0, "url-validation", this.getMessages());
   }
}
