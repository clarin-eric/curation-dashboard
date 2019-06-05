package eu.clarin.helpers;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import eu.clarin.curation.linkchecker.urlElements.URLElement;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

//this class connects with mongodb and creates a html report out of the queries
public class LinkCheckerStatisticsHelper {
    private static final Logger _logger = LoggerFactory.getLogger(LinkCheckerStatisticsHelper.class);

    private static final MongoClient mongoClient;
    private MongoCollection<Document> linksChecked;

    private DecimalFormat numberFormatter = new DecimalFormat("###,###.##");

    static { //since MongoClient is already a connection pool only one instance should exist in the application
        if (Configuration.DATABASE) {
            _logger.info("Connecting to database...");
            if (Configuration.DATABASE_URI == null || Configuration.DATABASE_URI.isEmpty()) {//if it is empty, try localhost
                mongoClient = MongoClients.create();
            } else {
                mongoClient = MongoClients.create(Configuration.DATABASE_URI);
            }
        } else {
            mongoClient = null;
        }
    }

    public LinkCheckerStatisticsHelper() {

        MongoDatabase database = mongoClient.getDatabase(Configuration.DATABASE_NAME);
        _logger.info("Connected to database.");

        this.linksChecked = database.getCollection("linksChecked");
    }


    private AggregateIterable<Document> getStatusStatistics() {

        return linksChecked.aggregate(Arrays.asList(
                Aggregates.group("$status",
                        Accumulators.sum("count", 1),
                        Accumulators.avg("avg_resp", "$duration"),
                        Accumulators.max("max_resp", "$duration")
                ),
                Aggregates.sort(orderBy(ascending("_id")))
        ));

    }

    private AggregateIterable<Document> getStatusStatistics(String collectionName) {
        if (collectionName.equals("Overall")) {
            return getStatusStatistics();
        }

        return linksChecked.aggregate(Arrays.asList(
                Aggregates.match(eq("collection", collectionName)),
                Aggregates.group("$status",
                        Accumulators.sum("count", 1),
                        Accumulators.avg("avg_resp", "$duration"),
                        Accumulators.max("max_resp", "$duration")
                ),
                Aggregates.sort(orderBy(ascending("_id")))
        ));
    }

    private AggregateIterable<Document> getStatusStatisticsTotal() {

        return linksChecked.aggregate(Arrays.asList(
                Aggregates.group("_id",
                        Accumulators.sum("count", 1)
                )
        ));

    }

    private AggregateIterable<Document> getStatusStatisticsTotal(String collectionName) {
        if (collectionName.equals("Overall")) {
            return getStatusStatisticsTotal();
        }

        return linksChecked.aggregate(Arrays.asList(
                Aggregates.match(eq("collection", collectionName)),
                Aggregates.group("_id",
                        Accumulators.sum("count", 1)
                )
        ));

    }

    //this method doesn't take 0 status codes into consideration. 0 means there was an error in the URL and there was no request sent.
    private AggregateIterable<Document> getStatusStatisticsAvg() {

        return linksChecked.aggregate(Arrays.asList(
                Aggregates.match(not(eq("status", 0))),
                Aggregates.group("_id",
                        Accumulators.avg("avg_resp", "$duration")
                )
        ));
    }


    //this method doesn't take 0 status codes into consideration. 0 means there was an error in the URL and there was no request sent.
    private AggregateIterable<Document> getStatusStatisticsAvg(String collectionName) {
        if (collectionName.equals("Overall")) {
            return getStatusStatisticsAvg();
        }

        return linksChecked.aggregate(Arrays.asList(
                Aggregates.match(and(eq("collection", collectionName), not(eq("status", 0)))),
                Aggregates.group("_id",
                        Accumulators.avg("avg_resp", "$duration")
                )
        ));

    }

    public String createURLTable(String collectionName, int status) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div>");
        sb.append("<h1>Link Checking Statistics:</h1>");
        sb.append("<h3>").append(collectionName).append(":</h3>");

        List<String> columnNames = Arrays.asList("Url", "Message", "Info", "Record");

        sb.append("<table class='reportTable' id='statsTable' data-collection='" + collectionName + "' data-status='" + status + "'>");
        sb.append("<thead>");
        sb.append("<tr>");
        for (String columnName : columnNames) {
            sb.append("<th>").append(columnName).append("</th>");
        }
        sb.append("</tr>");

        sb.append("</thead>");
        sb.append("<tbody id='reportTableTbody'>");

        sb.append(getHtmlRowsInBatch(collectionName, status, 0));

        sb.append("</tbody>");
        sb.append("</table>");
        sb.append("<div id='tableEndSpan' class='centerContainer'></div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }

    public String getHtmlRowsInBatch(String collectionName, int status, int batchCount) {

        int start = batchCount * 100;
        int end = start + 100;

        StringBuilder sb = new StringBuilder();

        MongoCursor<Document> cursor;
        if (collectionName.equals("Overall")) {
            cursor = linksChecked.find(eq("status", status)).skip(start).limit(end).iterator();
        } else {
            cursor = linksChecked.find(and(eq("status", status), eq("collection", collectionName))).skip(batchCount * 100).limit(batchCount * 100 + 100).iterator();
        }
        try {
            while (cursor.hasNext()) {


                sb.append("<tr>");
                Document doc = cursor.next();
                URLElement urlElement = new URLElement(doc);

                sb.append("<td>");
                sb.append("<a href='").append(urlElement.getUrl()).append("'>").append(urlElement.getUrl()).append("</a>");
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getMessage());
                sb.append("</td>");

                //button
                sb.append("<td>");
                sb.append("<button class='showUrlInfo btn btn-info'>Show</button>");
                sb.append("</td>");

                sb.append("<td>");
                //some html css table too wide work around
                String record = urlElement.getRecord();
                if (record != null) {
                    record = record.replace("_", "_<wbr>");
                    sb.append(record);
                }

                sb.append("</tr>");

                //info
                sb.append("<tr hidden><td colspan='4'>");

                //because this field is new, older entries dont have it and it results in null, so a null check to make it more user friendly
                String expectedContent = urlElement.getExpectedMimeType() == null ? "Not Specified" : urlElement.getExpectedMimeType();
                String content = urlElement.getContentType();

                sb.append("<b>Expected Content Type: </b>").append(expectedContent).append("<br>");
                sb.append("<b>Content Type: </b>").append(content).append("<br>");
                sb.append("<b>Byte Size: </b>").append(urlElement.getByteSize()).append("<br>");
                sb.append("<b>Request Duration(ms): </b>").append(urlElement.getDuration()).append("<br>");

                String method = urlElement.getMethod()==null?"N/A":urlElement.getMethod();
                sb.append("<b>Method: </b>").append(method).append("<br>");
                sb.append("<b>Timestamp: </b>").append(TimeUtils.humanizeToDate(urlElement.getTimestamp()));
                sb.append("</td>");
                //info end

                sb.append("</td></tr>");
            }
        } finally {
            cursor.close();
        }


        return sb.toString();
    }
}
