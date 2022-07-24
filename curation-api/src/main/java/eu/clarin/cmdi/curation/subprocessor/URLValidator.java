package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.cpa.model.Client;
import eu.clarin.cmdi.cpa.model.Status;
import eu.clarin.cmdi.cpa.model.Url;
import eu.clarin.cmdi.cpa.repository.ClientRepository;
import eu.clarin.cmdi.cpa.repository.StatusRepository;
import eu.clarin.cmdi.cpa.repository.UrlRepository;
import eu.clarin.cmdi.cpa.service.LinkService;
import eu.clarin.cmdi.curation.configuration.CurationConfig;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.URLReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class URLValidator extends CMDSubprocessor {

   @Autowired
   private CurationConfig conf;
   @Autowired
   private LinkService uService;
   @Autowired
   private UrlRepository uRepository;
   @Autowired
   private StatusRepository sRepository;
   @Autowired
   private ClientRepository clRepository;

   @Override
   public void process(CMDInstance entity, CMDInstanceReport report) {
      // do nothing this is not used, not really good coding, is it???
   }

   public void process(CMDInstance entity, CMDInstanceReport report, String parentName) {

      CMDIData<Map<String, List<ValueSet>>> data = entity.getCMDIData();

      Map<String, Resource> urlMap = new HashMap<>();

      final AtomicLong numOfLinks = new AtomicLong(0); // has to be final for use in lambda expression

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
               numOfLinks.incrementAndGet();
            });

      String selfLink = (data.getDocument().get("_selfLink") != null && !data.getDocument().get("_selfLink").isEmpty())
            ? data.getDocument().get("_selfLink").get(0).getValue()
            : "";

      // only add selfLink if url
      if (selfLink.startsWith("http://") || selfLink.startsWith("https://"))
         urlMap.computeIfAbsent(selfLink, key -> null);

      // these values are used to create instance url report and used for the score
      // collection statistics are done through the database
      long numOfUniqueLinks = urlMap.keySet().size();
      long numOfCheckedLinks = 0;
      long numOfBrokenLinks = 0;
      long numOfUndeterminedLinks = 0;
      long numOfRestrictedAccessLinks = 0;
      long numOfBlockedByRobotsTxtLinks = 0;
      long numOfInvalidLinks = 0;

      if (conf.getMode().equals("collection")) {
         
         Client client = clRepository.findByEmailAndToken("", "");


         for (String url : urlMap.keySet()) {

            String origin = report.getName();
            String providergroup = parentName != null ? parentName : origin;

            String expectedMimeType = urlMap.get(url).getMimeType();
            expectedMimeType = expectedMimeType == null ? "Not Specified" : expectedMimeType;

            uService.save(client, url, origin, providergroup, expectedMimeType);
         }
      }
      else {// instance mode
         for (String url : urlMap.keySet()) {

            // first check if database has this url
            Url urlEntity = uRepository.findByName(url);
            Status statusEntity;
            
            if((urlEntity = uRepository.findByName(url)) != null && (statusEntity = sRepository.findByUrl(urlEntity)) != null) {
               numOfCheckedLinks++;
               
               switch(statusEntity.getCategory()) {
               case Ok:
                  break;

               case Broken:
                  addMessage(Severity.ERROR, "Url: " + urlEntity.getName() + " Category:" + statusEntity.getCategory());
                  numOfBrokenLinks++;
                  break;
                  
               case Invalid_URL:
                  addMessage(Severity.ERROR, "Url: " + urlEntity.getName() + " Category:" + statusEntity.getCategory());
                  numOfInvalidLinks++;
                  break;

               case Undetermined:
                  addMessage(Severity.WARNING, "Url: " + urlEntity.getName() + " Category:" + statusEntity.getCategory());
                  numOfUndeterminedLinks++;
                  break;

               case Restricted_Access:
                  addMessage(Severity.WARNING, "Url: " + urlEntity.getName() + " Category:" + statusEntity.getCategory());
                  numOfRestrictedAccessLinks++;
                  break;

               case Blocked_By_Robots_txt: 
                  addMessage(Severity.WARNING, "Url: " + urlEntity.getName() + " Category:" + statusEntity.getCategory());
                  numOfBlockedByRobotsTxtLinks++;
               }
               
               CMDInstanceReport.URLElement urlElementReport = new CMDInstanceReport.URLElement()
                     .convertFromLinkCheckerURLElement(statusEntity);
               report.addURLElement(urlElementReport);
            }
         }
      }
      report.urlReport = createInstanceURLReport(numOfLinks.get(), numOfUniqueLinks, numOfCheckedLinks,
            numOfBrokenLinks, numOfUndeterminedLinks, numOfRestrictedAccessLinks, numOfBlockedByRobotsTxtLinks);

   }

   public Score calculateScore(CMDInstanceReport report) {
      // it can influence the score, if one collection was done with enabled and the
      // other without

      double score = report.urlReport.percOfValidLinks != null && !Double.isNaN(report.urlReport.percOfValidLinks)
            ? report.urlReport.percOfValidLinks
            : 0;
      return new Score(score, 1.0, "url-validation", msgs);
   }

   private URLReport createInstanceURLReport(long numOfLinks, long numOfUniqueLinks, long numOfCheckedLinks,
         long numOfBrokenLinks, long numOfUndeterminedLinks, long numOfRestrictedAccessLinks,
         long numOfBlockedByRobotsTxtLinks) {
      URLReport report = new URLReport();
      report.numOfLinks = numOfLinks;

      // all links are checked in instances
      report.numOfCheckedLinks = numOfCheckedLinks;
      report.numOfUndeterminedLinks = numOfUndeterminedLinks;
      report.numOfRestrictedAccessLinks = numOfRestrictedAccessLinks;
      report.numOfBlockedByRobotsTxtLinks = numOfBlockedByRobotsTxtLinks;
      report.numOfUniqueLinks = numOfUniqueLinks;
      report.numOfBrokenLinks = numOfBrokenLinks;

      long numOfCheckedUndeterminedRemoved = numOfLinks - numOfUndeterminedLinks;
      report.percOfValidLinks = numOfLinks == 0 ? 0
            : (numOfCheckedUndeterminedRemoved - numOfBrokenLinks) / (double) numOfCheckedUndeterminedRemoved;

      return report;
   }
}
