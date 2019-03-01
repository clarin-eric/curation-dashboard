package eu.clarin.web.utils;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import eu.clarin.curation.linkchecker.urlElements.URLElement;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        //ensure indexes to speed up queries later
//        database.getCollection("linksChecked").createIndex(Indexes.ascending("record"));
//        database.getCollection("linksChecked").createIndex(Indexes.ascending("collection"));
//        database.getCollection("linksChecked").createIndex(Indexes.ascending("status"));
//        database.getCollection("linksChecked").createIndex(Indexes.ascending("record", "status"));
//        database.getCollection("linksChecked").createIndex(Indexes.ascending("collection", "status"));
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
        if(collectionName.equals("Overall")){
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
        if(collectionName.equals("Overall")){
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
        if(collectionName.equals("Overall")){
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

        sb.append("<html><head>\n" +
                "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"./VAADIN/themes/mytheme/xsltStyle.css?v=7.6.4\" />\n" +
                "\t</head><body>");

        sb.append("<div>");
        sb.append("<h1>Link Checking Statistics:</h1>");
        sb.append("<h3>").append(collectionName).append(" (limited to first 100 URLs)").append(":</h3>");

        MongoCursor<Document> cursor;
        if (collectionName.equals("Overall")) {
            cursor = linksChecked.find(eq("status", status)).limit(100).iterator();
        } else {
            cursor = linksChecked.find(and(eq("status", status), eq("collection", collectionName))).limit(100).iterator();
        }

        List<String> columnNames = Arrays.asList("Url", "Message", "Http Status", "Content-Type", "Expected Content-Type", "Byte-Size", "Request Duration(ms)", "Timestamp", "Method", "Redirect Count", "Record");

        sb.append("<table>");
        sb.append("<thead>");
        sb.append("<tr>");
        for (String columnName : columnNames) {
            sb.append("<th>").append(columnName).append("</th>");
        }
        sb.append("</tr>");

        sb.append("</thead>");
        sb.append("<tbody>");

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
                sb.append("<td>");
                sb.append(urlElement.getStatus());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getContentType());
                sb.append("</td>");
                sb.append("<td>");
                //because this field is new, older entries dont have it and it results in null, so a null check to make it more user friendly
                sb.append(urlElement.getExpectedMimeType() == null ? "Not Specified" : urlElement.getExpectedMimeType());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getByteSize());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getDuration());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(TimeUtils.humanizeToDate(urlElement.getTimestamp()));
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getMethod());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getRedirectCount());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getRecord());
                sb.append("</td>");

                sb.append("</tr>");
            }
        } finally {
            cursor.close();
        }


        sb.append("</tbody>");
        sb.append("</table>");

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private class StatisticRow {
        private int status;
        private String category;
        private int count;
        private double avgResp;
        private long maxResp;
    }

    public String createHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>\n" +
                "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"./VAADIN/themes/mytheme/xsltStyle.css?v=7.6.4\" />\n" +
                "\t</head><body>");

        sb.append("<div>");
        sb.append("<h1>Link Checking Statistics:</h1>");
        //general table
        addTable("Overall",sb);

        sb.append("<h2>Collections:</h2>");

        //for each collection
        //each collection statistics
        try (Stream<Path> paths = Files.walk(Paths.get(Configuration.OUTPUT_DIRECTORY.toString() + "/collections/"))) {

            List<Path> files = paths.filter(Files::isRegularFile).collect(Collectors.toList());

            for (Path path : files) {

                String collectionName = path.getFileName().toString().split("\\.")[0];

                addTable(collectionName,sb);

            }

        } catch (IOException e) {
            _logger.error("Can't get the filenames list of collection reports.");
            //don't put those in the html
        }

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private void addTable(String collectionName, StringBuilder sb) {
        List<StatisticRow> rows = new ArrayList<>();

        AggregateIterable<Document> iterable = getStatusStatistics(collectionName);

        boolean empty = true;
        for (Document doc : iterable) {
            if (empty) {
                empty = false;
            }

            StatisticRow row = new StatisticRow();

            Integer status = doc.getInteger("_id");
            row.status = status;
            if (status == 200) {
                row.category = "Ok";
            } else if (status == 401 || status == 405 || status == 429) {
                row.category = "Undetermined";
            } else {
                row.category = "Broken";
            }
            row.count = doc.getInteger("count");
            row.avgResp = doc.getDouble("avg_resp");
            row.maxResp = doc.getLong("max_resp");

            rows.add(row);
        }

        rows.sort(Comparator.comparing(statisticRow -> statisticRow.category));

        if (!empty) {
            int total = 0;
            double avgResp = 0.0;

            iterable = getStatusStatisticsTotal(collectionName);
            for (Document doc : iterable) {
                //there is only one document
                total = doc.getInteger("count");
            }

            iterable = getStatusStatisticsAvg(collectionName);
            for (Document doc : iterable) {
                //there is only one document
                avgResp = doc.getDouble("avg_resp");
            }

            sb.append(createStatisticsTable(collectionName, rows, total, avgResp));
        }
    }

    private String createStatisticsTable(String collectionName, List<StatisticRow> rows, int total, double avgResp) {
        List<String> columnNames = Arrays.asList("Status", "Category", "Count", "Average Response Duration(ms)", "Max Response Duration(ms)");

        StringBuilder sb = new StringBuilder();
        sb.append("<h3>").append(collectionName).append(":</h3>");
        sb.append("<table>");
        sb.append("<thead>");
        sb.append("<tr>");
        for (String columnName : columnNames) {
            sb.append("<th>").append(columnName).append("</th>");
        }
        sb.append("</tr>");

        sb.append("</thead>");
        sb.append("<tbody>");

        for (StatisticRow row : rows) {
            String element = "<a href='#!ResultView/statistics//" + collectionName + "/" + row.status + "'>" + numberFormatter.format(row.status) + "</a>";

            sb.append("<tr>");
            String color;

            if(row.category.equals("Ok")){
                color="#cbe7cc";
            }else if(row.category.equals("Undetermined")){
                color="#fff7b3";
            }else{
                color="#f2a6a6";
            }

            sb.append("<td align='right' style='background-color:").append(color).append("'>");
            sb.append(element);
            sb.append("</td>");
            sb.append("<td align='right' style='background-color:").append(color).append("'>");
            sb.append(row.category);
            sb.append("</td>");

            sb.append("<td align='right'>");
            sb.append(numberFormatter.format(row.count));
            sb.append("</td>");

            sb.append("<td align='right'>");
            sb.append(numberFormatter.format(row.avgResp));
            sb.append("</td>");

            sb.append("<td align='right'>");
            sb.append(numberFormatter.format(row.maxResp));
            sb.append("</td>");

            sb.append("</tr>");
        }
        sb.append("</tbody>");
        sb.append("</table>");

        sb.append("<b>Total Count: </b>").append(numberFormatter.format(total)).append("<br>");
        sb.append("<b>Average Response Duration(ms): </b>").append(numberFormatter.format(avgResp));

        return sb.toString();
    }

}
