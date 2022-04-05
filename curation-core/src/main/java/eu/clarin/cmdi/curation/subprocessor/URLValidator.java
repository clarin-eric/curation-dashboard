package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.URLReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.DAO.LinkToBeChecked;
import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class URLValidator extends CMDSubprocessor {

   private static final Logger LOG = LoggerFactory.getLogger(URLValidator.class);

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

      if (Configuration.COLLECTION_MODE) {
         
         List<LinkToBeChecked> linkList = new ArrayList<LinkToBeChecked>();

         for (String url : urlMap.keySet()) {

            String finalRecord = report.getName();
            String finalCollection = parentName != null ? parentName : finalRecord;

            String expectedMimeType = urlMap.get(url).getMimeType();
            expectedMimeType = expectedMimeType == null ? "Not Specified" : expectedMimeType;

            linkList.add(
                  new LinkToBeChecked(url, Configuration.LINK_DATA_SOURCE, finalRecord, finalCollection, expectedMimeType,
                        Configuration.REPORT_GENERATION_DATE)
               );
         }
         
         try {// save links
            Configuration.linkToBeCheckedResource.save(linkList);
         }
         catch (SQLException e) {
            LOG.error("Error when saving link list" + linkList.stream().map(LinkToBeChecked::toString).collect(Collectors.joining(System.lineSeparator())) + ": " + e.getMessage(), e);
         }
      }
      else {// instance mode
         for (String url : urlMap.keySet()) {
            // first check if database has this url
            Optional<CheckedLink> checkedLinkOptional = null;
            CheckedLink checkedLink = new CheckedLink();
            try (Stream<CheckedLink> stream = Configuration.checkedLinkResource
                  .get(Configuration.checkedLinkResource.getCheckedLinkFilter().setUrlIn(url))) {
               checkedLinkOptional = stream.findFirst();
            }
            catch (SQLException e) {
               // error doesn't matter, treat it as if it is not in the database
            }
            if (checkedLinkOptional != null && !checkedLinkOptional.isEmpty()) {
               numOfCheckedLinks++;
               checkedLink = checkedLinkOptional.get();
               /*
                * } else { try { checkedLink = httpLinkChecker.checkLink(url);//redirect follow
                * level is current level, because this is the first request it is set to 0 }
                * catch (Exception e) { checkedLink.setUrl(url); //ugly hack: category enum in
                * both projects is the same. cant use one in both because java versions dont
                * match. need to update stormychecker to java 11
                * checkedLink.setCategory(Category.valueOf(CategoryException.
                * getCategoryFromException(e, url).name()));
                * checkedLink.setMessage(e.getMessage()); checkedLink.setStatus(null);
                * checkedLink.setByteSize(0); checkedLink.setTimestamp(new
                * Timestamp(System.currentTimeMillis())); checkedLink.setDuration(0);
                * checkedLink.setContentType(null); checkedLink.setMethod("HEAD");//first HEAD
                * request will produce the exception }
                * 
                * String expectedMimeType = urlMap.get(url).getMimeType(); expectedMimeType =
                * expectedMimeType == null ? "Not Specified" : expectedMimeType;
                * checkedLink.setExpectedMimeType(expectedMimeType);
                * 
                * checkedLink.setRecord(report.getName()); }
                */
               Category category = checkedLink.getCategory();
               if (category.equals(Category.Ok)) {
                  // do nothing
               }
               else if (category.equals(Category.Broken)) {
                  addMessage(Severity.ERROR, "Url: " + checkedLink.getUrl() + " Category:" + category);
                  numOfBrokenLinks++;
               }
               else if (category.equals(Category.Undetermined)) {
                  addMessage(Severity.WARNING, "Url: " + checkedLink.getUrl() + " Category:" + category);
                  numOfUndeterminedLinks++;
               }
               else if (category.equals(Category.Restricted_Access)) {
                  addMessage(Severity.WARNING, "Url: " + checkedLink.getUrl() + " Category:" + category);
                  numOfRestrictedAccessLinks++;
               }
               else {// blocked by robots.txt
                  addMessage(Severity.WARNING, "Url: " + checkedLink.getUrl() + " Category:" + category);
                  numOfBlockedByRobotsTxtLinks++;
               }

               CMDInstanceReport.URLElement urlElementReport = new CMDInstanceReport.URLElement()
                     .convertFromLinkCheckerURLElement(checkedLink);
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
