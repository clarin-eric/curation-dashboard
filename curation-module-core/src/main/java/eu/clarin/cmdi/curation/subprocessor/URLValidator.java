package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.URLReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import eu.clarin.curation.linkchecker.httpLinkChecker.HTTPLinkChecker;
import eu.clarin.curation.linkchecker.urlElements.URLElement;
import eu.clarin.curation.linkchecker.urlElements.URLElementToBeChecked;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dostojic
 */

public class URLValidator extends CMDSubprocessor {

    private static final Logger _logger = LoggerFactory.getLogger(URLValidator.class);

    private static final MongoCollection<Document> linksToBeChecked;
    private static final MongoCollection<Document> linksChecked;

    static { //since MongoClient is already a connection pool only one instance should exist in the application

        if (Configuration.DATABASE) {
            MongoClient mongoClient; 
            
            _logger.info("Connecting to database...");
            if (Configuration.DATABASE_URI == null || Configuration.DATABASE_URI.isEmpty()) {//if it is empty, try localhost
                mongoClient = MongoClients.create();
            } else {
                mongoClient = MongoClients.create(Configuration.DATABASE_URI);
            }
            
            MongoDatabase database = mongoClient.getDatabase(Configuration.DATABASE_NAME);
            _logger.info("Connected to database.");

            //get links from linksToBeChecked
            linksToBeChecked = database.getCollection("linksToBeChecked");

            //get linksChecked
            linksChecked = database.getCollection("linksChecked");
        } else {
            linksToBeChecked = null;
            linksChecked = null;
            _logger.info("Configuration.DATABASE=false - therefore no database connection established");

        }


    }

    @Override
    public void process(CMDInstance entity, CMDInstanceReport report) {
        //do nothing this is not used
    }

    public void process(CMDInstance entity, CMDInstanceReport report, String parentName) {

        CMDIData<Map<String, List<ValueSet>>> data = entity.getCMDIData();
        
        Map<String,Resource> urlMap = new HashMap<String,Resource>();
        
        final AtomicLong numOfLinks = new AtomicLong(0);  //has to be final for use in lambda expression
        
        ArrayList<Resource> resources = new ArrayList<Resource>();
                resources.addAll(data.getDataResources());
                resources.addAll(data.getLandingPageResources());
                resources.addAll(data.getMetadataResources());
                resources.addAll(data.getSearchPageResources());
                resources.addAll(data.getSearchResources());
        
        resources.stream()
            .filter(resource -> resource.getResourceName() != null && resource.getResourceName().startsWith("http"))
            .forEachOrdered(resource -> {
                urlMap.computeIfAbsent(resource.getResourceName(), key -> resource);
                numOfLinks.incrementAndGet();
            });
        
        data.getDocument().values().stream()
            .forEach(valuesList -> valuesList.stream().filter(valueSet -> valueSet.getValue().startsWith("http")).forEach(valueSet -> urlMap.computeIfAbsent(valueSet.getValue(), key -> null))
                );
        

        if (Configuration.HTTP_VALIDATION) {
            if (Configuration.DATABASE && Configuration.COLLECTION_MODE) {

                urlMap.keySet().stream().forEach(url -> {

                    _logger.info("Checking database for url: " + url);

                    Bson filter = Filters.eq("url", url);
                    MongoCursor<Document> cursor = linksChecked.find(filter).iterator();

                    //because urls are unique in the database if cursor has next, it found the only one. If not, the url wasn't found.
                    if (cursor.hasNext()) {
                        URLElement urlElement = new URLElement(cursor.next());

                        addMessageForStatusCode(urlElement.getStatus(), url);

//                        CMDInstanceReport.URLElement urlElementReport = new CMDInstanceReport.URLElement().convertFromLinkCheckerURLElement(urlElement);
//                        report.addURLElement(urlElementReport);

                    } else {

                        String finalCollection = parentName;
                        String finalRecord = report.getName();

                        if (finalCollection == null) {
                            finalCollection = finalRecord;
                        }

                        URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(url, finalRecord, finalCollection);

                        try {

                            linksToBeChecked.insertOne(urlElementToBeChecked.getMongoDocument());
                        } catch (MongoException e) {
                            //duplicate key error
                            //the url is already in the linksToBeChecked, do nothing
                        }


                    }
                });

                report.urlReport = createURLReport(numOfLinks.get(), urlMap.size(), report.getName());

            } else {
                HTTPLinkChecker httpLinkChecker = new HTTPLinkChecker(Configuration.TIMEOUT, Configuration.REDIRECT_FOLLOW_LIMIT, Configuration.USERAGENT);

                urlMap.keySet().stream().forEach(url -> {

                    try {// check if URL is broken
                        _logger.info("Checking url: " + url);

                        URLElement urlElement = httpLinkChecker.checkLink(url, 0, 0, url);//redirect follow level is current level, because this is the first request it is set to 0

                        addMessageForStatusCode(urlElement.getStatus(), url);

                        CMDInstanceReport.URLElement urlElementReport = new CMDInstanceReport.URLElement().convertFromLinkCheckerURLElement(urlElement);
                        report.addURLElement(urlElementReport);


                    } catch (IOException e) {
                        CMDInstanceReport.URLElement urlElement = new CMDInstanceReport.URLElement();
                        urlElement.message = e.getLocalizedMessage();
                        urlElement.url = url;
                        urlElement.status = 0;
                        urlElement.contentType = null;
                        urlElement.byteSize = "0";
                        urlElement.timestamp = TimeUtils.humanizeToDate(System.currentTimeMillis());
                        urlElement.duration = "0 ms";
                        urlElement.redirectCount = 0;
                        report.addURLElement(urlElement);


                        addMessage(Severity.ERROR, "URL: " + url + "    STATUS:" + e.toString());
                    }

                });
                report.urlReport = createURLReport(numOfLinks.get(), urlMap.size(), report.getName());

            }

        } else {

            report.urlReport = createURLReport(numOfLinks.get(), urlMap.size(), report.getName());

            addMessage(Severity.INFO, "Link validation is disabled");
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

    @Override
    public Score calculateScore(CMDInstanceReport report) {
        // it can influence the score, if one collection was done with enabled and the other without

        double score = report.urlReport.percOfValidLinks != null && !Double.isNaN(report.urlReport.percOfValidLinks) ? report.urlReport.percOfValidLinks : 0;
        return new Score(score, 1.0, "url-validation", msgs);
    }


    private URLReport createURLReport(long numOfLinks, long numOfUniqueLinks, String name) {
        URLReport report = new URLReport();
        report.numOfLinks = numOfLinks;
        report.numOfUniqueLinks = numOfUniqueLinks;

        if (Configuration.HTTP_VALIDATION) {

            Bson checkedLinksFilter = Filters.eq("record", name);
            long numOfCheckedLinks = linksChecked.countDocuments(checkedLinksFilter);

            Bson brokenLinksFilter = Filters.and(Filters.eq("record", name), Filters.and(Filters.not(Filters.eq("status", 200)), Filters.not(Filters.eq("status", 302))));
            long numOfBrokenLinks = linksChecked.countDocuments(brokenLinksFilter);

            report.percOfValidLinks = numOfCheckedLinks == 0 ? 0 : (numOfCheckedLinks - numOfBrokenLinks) / (double) numOfCheckedLinks;
        }
        return report;
    }

}
