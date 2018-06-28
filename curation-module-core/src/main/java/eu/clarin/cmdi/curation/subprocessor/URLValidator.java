package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import urlElements.URLElement;

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
            AtomicInteger numOfBrokenLinks = new AtomicInteger(0);
            if (Configuration.DATABASE) {
//todo
                //check in linkschecked collection for link
                //if exists, create cmdiinstance.urlelement
                //if not, add to linksToBeChecked and add to report as unchecked link(how to do this?)
                //and also dont forget to do numofbrokenlinks.incrementandget and other number operations accordingly
            } else {

                links.stream().forEach(url -> {

                    try {// check if URL is broken
                        logger.info("Checking url: " + url);

                        URLElement urlElement = new HTTPLinkChecker().checkLink(url, 0, 0);//redirect follow level is current level, because this is the first request it is set to 0
                        int responseCode = urlElement.getStatus();
                        if (responseCode == 200 || responseCode == 302) {
                        } // OK
                        else if (responseCode < 400) {// 2XX and 3XX, redirections, empty content
                            addMessage(Severity.WARNING, "URL: " + url + "     STATUS:" + responseCode);
                        } else {// 4XX and 5XX, client/server errors
                            numOfBrokenLinks.incrementAndGet();
                            addMessage(Severity.ERROR, "URL: " + url + "    STATUS:" + responseCode);
                        }

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
                });
            }
            report.urlReport = createURLReport(numOfLinks, numOfBrokenLinks.get(), numOfUniqueLinks);
        } else {
            report.urlReport = createURLReport(numOfLinks, 0, numOfUniqueLinks);
            addMessage(Severity.INFO, "Link validation is disabled");
        }

    }

    @Override
    public Score calculateScore(CMDInstanceReport report) {
        // it can influence the score, if one collection was done with enabled and the other without

        double score = report.urlReport.percOfValidLinks != null && !Double.isNaN(report.urlReport.percOfValidLinks) ? report.urlReport.percOfValidLinks : 0;
        return new Score(score, 1.0, "url-validation", msgs);
    }

    private URLReport createURLReport(int numOfLinks, int numOfBrokenLinks, int numOfUniqueLinks) {
        URLReport report = new URLReport();
        report.numOfLinks = numOfLinks;
        report.numOfBrokenLinks = numOfBrokenLinks;
        report.numOfUniqueLinks = numOfUniqueLinks;
        if (Configuration.HTTP_VALIDATION) {
            report.percOfValidLinks = (numOfUniqueLinks - numOfBrokenLinks) / (double) numOfUniqueLinks;
        }
        return report;
    }

}
