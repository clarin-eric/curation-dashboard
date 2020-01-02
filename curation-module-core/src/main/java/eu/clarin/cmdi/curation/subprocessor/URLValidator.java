package eu.clarin.cmdi.curation.subprocessor;


import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.URLReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.utils.HTTPLinkChecker;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.DAO.LinkToBeChecked;
import eu.clarin.cmdi.rasa.filters.impl.ACDHStatisticsCountFilter;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class URLValidator extends CMDSubprocessor {

    private static final Logger _logger = LoggerFactory.getLogger(URLValidator.class);


    @Override
    public void process(CMDInstance entity, CMDInstanceReport report) {
        //do nothing this is not used, not really good coding is it???
    }

    public void process(CMDInstance entity, CMDInstanceReport report, String parentName) {

        CMDIData<Map<String, List<ValueSet>>> data = entity.getCMDIData();

        Map<String, Resource> urlMap = new HashMap<String, Resource>();

        final AtomicLong numOfLinks = new AtomicLong(0);  //has to be final for use in lambda expression

        ArrayList<Resource> resources = new ArrayList<Resource>();
        resources.addAll(data.getDataResources());
        resources.addAll(data.getLandingPageResources());
        resources.addAll(data.getMetadataResources());
        resources.addAll(data.getSearchPageResources());
        resources.addAll(data.getSearchResources());

        resources.stream()
                .filter(resource -> resource.getResourceName() != null && (resource.getResourceName().startsWith("http://") || resource.getResourceName().startsWith("https://")))
                .forEach(resource -> {
                    urlMap.computeIfAbsent(resource.getResourceName(), key -> resource);
                    numOfLinks.incrementAndGet();
                });

        String selfLink = (data.getDocument().get("_selfLink") != null && !data.getDocument().get("_selfLink").isEmpty()) ? data.getDocument().get("_selfLink").get(0).getValue() : "";

        //only add selfLink if url
        if (selfLink.startsWith("http://") || selfLink.startsWith("https://"))
            urlMap.computeIfAbsent(selfLink, key -> null);

        if (Configuration.COLLECTION_MODE) {

            for (String url : urlMap.keySet()) {

//                }
//                urlMap.keySet().parallelStream().forEach(url -> {

                //this causes performance problems
//                    _logger.info("Checking database for url: " + url);

                CheckedLink checkedLink = null;
                try {
                    checkedLink = Configuration.checkedLinkResource.get(url, parentName);

                    if (checkedLink == null) {
                        String expectedMimeType = urlMap.get(url).getMimeType();
                        expectedMimeType = expectedMimeType == null ? "Not Specified" : expectedMimeType;

                        String finalRecord = report.getName();
                        String finalCollection = parentName != null ? parentName : finalRecord;

                        LinkToBeChecked linkToBeChecked = new LinkToBeChecked(url, finalRecord, finalCollection, expectedMimeType);

                        Configuration.linkToBeCheckedResource.save(linkToBeChecked);


                    }//else dont do anything, it is already in linksChecked

                } catch (SQLException e) {
                    _logger.error("Error when getting " + url + " from status table or saving it to urls table: " + e.getMessage());
                }
            }//);

//                removeOldURLs(urlMap.keySet(), report.getName(), parentName);

            try {
                report.urlReport = createInstanceURLReportFromDatabase(numOfLinks.longValue(), report.getName(), parentName);
            } catch (SQLException e) {
                _logger.error("Error when creating collection url report for collection: " + report.getParentName() + ": " + e.getMessage());
            }


        } else {
            HTTPLinkChecker httpLinkChecker = new HTTPLinkChecker(Configuration.TIMEOUT, Configuration.REDIRECT_FOLLOW_LIMIT, Configuration.USERAGENT);

            long numOfUniqueLinks = urlMap.keySet().size();

            long numOfBrokenLinks = 0;
            long numOfUndeterminedLinks = 0;

            for (String url : urlMap.keySet()) {

                String expectedMimeType = urlMap.get(url).getMimeType();
                expectedMimeType = expectedMimeType == null ? "Not Specified" : expectedMimeType;

                try {// check if URL is broken
                    _logger.info("Checking url: " + url);

                    CheckedLink checkedLink = httpLinkChecker.checkLink(url, 0, 0, url);//redirect follow level is current level, because this is the first request it is set to 0
                    checkedLink.setExpectedMimeType(expectedMimeType);

                    if (checkedLink.getStatus() != 200 && checkedLink.getStatus() != 302) {
                        if (checkedLink.getStatus() == 401 || checkedLink.getStatus() == 405 || checkedLink.getStatus() == 429) {
                            numOfUndeterminedLinks++;
                        } else {
                            numOfBrokenLinks++;
                        }

                    }

                    addMessageForStatusCode(checkedLink.getStatus(), url);

                    CMDInstanceReport.URLElement urlElementReport = new CMDInstanceReport.URLElement().convertFromLinkCheckerURLElement(checkedLink);
                    report.addURLElement(urlElementReport);


                } catch (IOException | IllegalArgumentException e) {

                    numOfBrokenLinks++;

                    CMDInstanceReport.URLElement urlElement = new CMDInstanceReport.URLElement();
                    urlElement.expectedContentType = expectedMimeType;
                    urlElement.message = e.getLocalizedMessage();
                    urlElement.url = url;
                    urlElement.status = 0;
                    urlElement.category = "Broken";
                    urlElement.contentType = null;
                    urlElement.byteSize = "0";
                    urlElement.timestamp = TimeUtils.humanizeToDate(System.currentTimeMillis());
                    urlElement.duration = "0 ms";
                    urlElement.redirectCount = 0;
                    report.addURLElement(urlElement);


                    addMessage(Severity.ERROR, "URL: " + url + "    STATUS:" + e.toString());
                }

            }

            report.urlReport = createInstanceURLReport(numOfLinks.get(), numOfUniqueLinks, numOfBrokenLinks, numOfUndeterminedLinks);

        }


    }

    private void addMessageForStatusCode(int responseCode, String url) {

        if (responseCode == 200 || responseCode == 302) {
        } // OK
        else if (responseCode < 400) {// 2XX and 3XX, redirections, empty content
            addMessage(Severity.WARNING, "URL: " + url + "     STATUS:" + responseCode);
        } else {// 4XX and 5XX, client/server errors
            addMessage(Severity.ERROR, "URL: " + url + "    STATUS:" + responseCode);
        }
    }

    public Score calculateScore(CMDInstanceReport report) {
        // it can influence the score, if one collection was done with enabled and the other without

        double score = report.urlReport.percOfValidLinks != null && !Double.isNaN(report.urlReport.percOfValidLinks) ? report.urlReport.percOfValidLinks : 0;
        return new Score(score, 1.0, "url-validation", msgs);
    }

    private URLReport createInstanceURLReport(long numOfLinks, long numOfUniqueLinks, long numOfBrokenLinks, long numOfUndeterminedLinks) {
        URLReport report = new URLReport();
        report.numOfLinks = numOfLinks;

        //all links are checked in instances
        report.numOfCheckedLinks = numOfLinks;
        report.numOfUndeterminedLinks = numOfUndeterminedLinks;
        report.numOfUniqueLinks = numOfUniqueLinks;
        report.numOfBrokenLinks = numOfBrokenLinks;

        long numOfCheckedUndeterminedRemoved = numOfLinks - numOfUndeterminedLinks;
        report.percOfValidLinks = numOfLinks == 0 ? 0 : (numOfCheckedUndeterminedRemoved - numOfBrokenLinks) / (double) numOfCheckedUndeterminedRemoved;

        return report;
    }

    private URLReport createInstanceURLReportFromDatabase(long numOfLinks, String instanceName, String collectionName) throws SQLException {
        URLReport report = new URLReport();
        report.numOfLinks = numOfLinks;

        ACDHStatisticsCountFilter filter = new ACDHStatisticsCountFilter(collectionName, instanceName);
        long numOfCheckedLinks = Configuration.statisticsResource.countStatusTable(Optional.of(filter));

        filter = new ACDHStatisticsCountFilter(collectionName, instanceName, true, false);
        long numOfBrokenLinks = Configuration.statisticsResource.countStatusTable(Optional.of(filter));

        filter = new ACDHStatisticsCountFilter(collectionName, instanceName, false, true);
        long numOfUndeterminedLinks = Configuration.statisticsResource.countStatusTable(Optional.of(filter));

        long numOfCheckedUndeterminedRemoved = numOfCheckedLinks - numOfUndeterminedLinks;
        report.percOfValidLinks = numOfCheckedLinks == 0 ? 0 : (numOfCheckedUndeterminedRemoved - numOfBrokenLinks) / (double) numOfCheckedUndeterminedRemoved;

        return report;
    }


    private URLReport createDisabledURLReport(long numOfLinks) {
        URLReport report = new URLReport();
        report.numOfLinks = numOfLinks;

        return report;
    }

}
