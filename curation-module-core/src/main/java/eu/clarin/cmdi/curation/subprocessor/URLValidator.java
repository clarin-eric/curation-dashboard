package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance.InstanceNode;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.URLReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import httpLinkChecker.HTTPLinkChecker;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import urlElements.URLElement;
import urlElements.URLElementToBeChecked;

/**
 * @author dostojic
 */

public class URLValidator extends CMDSubprocessor {

    private final static Logger logger = LoggerFactory.getLogger(URLValidator.class);

    @Override
    public void process(CMDInstance entity, CMDInstanceReport report) {
        ParsedInstance parsedInstance = entity.getParsedInstance();
        Collection<String> links = parsedInstance.getNodes()
                .stream()
                .filter(node -> !node.getXpath().equals("/CMD/@xsi:schemaLocation"))
                .filter(node -> !node.getXpath().equals("/CMD/@xmlns:xsi"))
                .filter(node -> !node.getXpath().equals("/CMD/@xml:xsi"))
                .map(InstanceNode::getValue)
                .filter(url -> url.startsWith("http"))
                .collect(Collectors.toList());
        int numOfLinks = links.size();
        links = links.stream().distinct().collect(Collectors.toList());
        int numOfUniqueLinks = links.size();

        //todo  and then add database checking before it, with database config variable

        // links are unique
        if (Configuration.HTTP_VALIDATION) {
            AtomicInteger numOfCheckedLinks = new AtomicInteger(0);
            AtomicInteger numOfBrokenLinks = new AtomicInteger(0);
            if (Configuration.DATABASE) {

                //connect to mongod and get database
                MongoDatabase database = getMongoDatabase();

                //get links from linksToBeChecked
                MongoCollection<Document> linksToBeChecked = database.getCollection("linksToBeChecked");

                //get linksChecked
                MongoCollection<Document> linksChecked = database.getCollection("linksChecked");

                links.stream().forEach(url -> {

                    logger.info("Checking database for url: " + url);

                    Bson filter = Filters.eq("url", url);
                    MongoCursor<Document> cursor = linksChecked.find(filter).iterator();

                    //because urls are unique in the database if cursor has next, it found the only one. If not, the url wasn't found.
                    if (cursor.hasNext()) {
                        URLElement urlElement = new URLElement(cursor.next());

                        addMessageForStatusCode(urlElement.getStatus(), numOfBrokenLinks, url);

                        CMDInstanceReport.URLElement urlElementReport = new CMDInstanceReport.URLElement().convertFromLinkCheckerURLElement(urlElement);
                        report.addURLElement(urlElementReport);
                        numOfCheckedLinks.incrementAndGet();

                    } else {
                        URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(url, report.getName());

                        try {

                            //TODO curation module populates with wrong collection names... todo in core module

                            linksToBeChecked.insertOne(urlElementToBeChecked.getMongoDocument());
                        } catch (MongoException e) {
                            //duplicate key error
                            //the url is already in the database, do nothing
                        }


                    }
                });

            } else {

                links.stream().forEach(url -> {

                    try {// check if URL is broken
                        logger.info("Checking url: " + url);

                        URLElement urlElement = new HTTPLinkChecker(Configuration.TIMEOUT, Configuration.REDIRECT_FOLLOW_LIMIT).checkLink(url, 0, 0, url);//redirect follow level is current level, because this is the first request it is set to 0

                        addMessageForStatusCode(urlElement.getStatus(), numOfBrokenLinks, url);

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
                        report.addURLElement(urlElement);

                        numOfBrokenLinks.incrementAndGet();
                        addMessage(Severity.ERROR, "URL: " + url + "    STATUS:" + e.toString());
                    }
                    numOfCheckedLinks.incrementAndGet();
                });

            }
            report.urlReport = createURLReport(numOfLinks, numOfBrokenLinks.get(), numOfUniqueLinks, numOfCheckedLinks.get());
        } else {
            report.urlReport = createURLReport(numOfLinks, 0, numOfUniqueLinks, 0);
            addMessage(Severity.INFO, "Link validation is disabled");
        }

    }

    private void addMessageForStatusCode(int responseCode, AtomicInteger numOfBrokenLinks, String url) {

        if (responseCode == 200 || responseCode == 302) {
        } // OK
        else if (responseCode < 400) {// 2XX and 3XX, redirections, empty content
            addMessage(Severity.WARNING, "URL: " + url + "     STATUS:" + responseCode);
        } else {// 4XX and 5XX, client/server errors
            numOfBrokenLinks.incrementAndGet();
            addMessage(Severity.ERROR, "URL: " + url + "    STATUS:" + responseCode);
        }
    }

    @Override
    public Score calculateScore(CMDInstanceReport report) {
        // it can influence the score, if one collection was done with enabled and the other without

        double score = report.urlReport.percOfValidLinks != null && !Double.isNaN(report.urlReport.percOfValidLinks) ? report.urlReport.percOfValidLinks : 0;
        return new Score(score, 1.0, "url-validation", msgs);
    }

    private static MongoDatabase getMongoDatabase() {
        logger.info("Connecting to database...");
        MongoClient mongoClient = MongoClients.create();

        MongoDatabase database = mongoClient.getDatabase(Configuration.DATABASE_NAME);
        logger.info("Connected to database.");
        return database;
    }

    private URLReport createURLReport(int numOfLinks, int numOfBrokenLinks, int numOfUniqueLinks, int numOfCheckedLinks) {
        URLReport report = new URLReport();
        report.numOfLinks = numOfLinks;
        report.numOfBrokenLinks = numOfBrokenLinks;
        report.numOfUniqueLinks = numOfUniqueLinks;
        report.numOfCheckedLinks = numOfCheckedLinks;
        if (Configuration.HTTP_VALIDATION) {
//            report.percOfValidLinks = (numOfUniqueLinks - numOfBrokenLinks) / (double) numOfUniqueLinks;
            //replace unique links with checked links so that the math only covers the checked links and doesn't display wrong results
            report.percOfValidLinks = numOfCheckedLinks == 0 ? 0 : (numOfCheckedLinks - numOfBrokenLinks) / (double) numOfCheckedLinks;
        }
        return report;
    }

}
