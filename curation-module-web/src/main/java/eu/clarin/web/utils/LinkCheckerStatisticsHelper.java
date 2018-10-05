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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

//this class connects with mongodb and creates a html report out of the queries
public class LinkCheckerStatisticsHelper {
    private static final Logger _logger = LoggerFactory.getLogger(LinkCheckerStatisticsHelper.class);

    private static final MongoClient mongoClient;
    private MongoCollection<Document> linksToBeChecked;
    MongoCollection<Document> linksChecked;

    DecimalFormat numberFormatter = new DecimalFormat("###,###.##");

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

        this.linksToBeChecked = database.getCollection("linksToBeChecked");
        this.linksChecked = database.getCollection("linksChecked");
    }


    private AggregateIterable<Document> getStatusStatistics() {
        AggregateIterable<Document> iterable = linksChecked.aggregate(Arrays.asList(
                Aggregates.group("$status",
                        Accumulators.sum("count", 1),
                        Accumulators.avg("avg_resp", "$duration"),
                        Accumulators.max("max_resp", "$duration")
                ),
                Aggregates.sort(orderBy(ascending("_id")))
        ));

        return iterable;

    }

    private AggregateIterable<Document> getStatusStatisticsTotal() {
        AggregateIterable<Document> iterable = linksChecked.aggregate(Arrays.asList(
                Aggregates.group("_id",
                        Accumulators.sum("count", 1),
                        Accumulators.avg("avg_resp", "$duration")
                )
        ));

        return iterable;

    }

    private AggregateIterable<Document> getStatusStatisticsTotal(String collectionName) {
        AggregateIterable<Document> iterable = linksChecked.aggregate(Arrays.asList(
                Aggregates.match(eq("collection", collectionName)),
                Aggregates.group("_id",
                        Accumulators.sum("count", 1)
                )
        ));

        return iterable;

    }

    //this method doensn't take 0 status codes into consideration. 0 means there was an error in the URL and there was no request sent.
    private AggregateIterable<Document> getStatusStatisticsAvg(String collectionName) {
        AggregateIterable<Document> iterable = linksChecked.aggregate(Arrays.asList(
                Aggregates.match(and(eq("collection", collectionName),not(eq("status",0)))),
                Aggregates.group("_id",
                        Accumulators.avg("avg_resp", "$duration")
                )
        ));

        return iterable;

    }

    private AggregateIterable<Document> getStatusStatistics(String collectionName) {
        AggregateIterable<Document> iterable = linksChecked.aggregate(Arrays.asList(
                Aggregates.match(eq("collection", collectionName)),
                Aggregates.group("$status",
                        Accumulators.sum("count", 1),
                        Accumulators.avg("avg_resp", "$duration"),
                        Accumulators.max("max_resp", "$duration")
                ),
                Aggregates.sort(orderBy(ascending("_id")))
        ));

        return iterable;
    }

    private AggregateIterable<Document> getLinksToBeCheckedStatistics() {
        AggregateIterable<Document> iterable = linksToBeChecked.aggregate(Arrays.asList(
                Aggregates.group("$collection",
                        Accumulators.sum("count", 1)
                )
        ));

        return iterable;
    }

    public String createURLTable(String collectionName, int status) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head>\n" +
                "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"./VAADIN/themes/mytheme/xsltStyle.css?v=7.6.4\" />\n" +
                "\t</head><body>");

        sb.append("<div>");
        sb.append("<h1>Link Checking Statistics:</h1>");
        sb.append("<h3>" + collectionName + " (limited to first 100 URLs)" + ":</h3>");

        MongoCursor<Document> cursor;
        if (collectionName.equals("Overall")) {
            cursor = linksChecked.find(eq("status", status)).limit(100).iterator();
        } else {
            cursor = linksChecked.find(and(eq("status", status), eq("collection", collectionName))).limit(100).iterator();
        }

        List<String> columnNames = Arrays.asList("Url", "Message", "Http Status", "Content-Type", "Byte-Size", "Request Duration", "Timestamp", "Method", "Redirect Count");

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
                sb.append(urlElement.getUrl());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getMessage());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(String.valueOf(urlElement.getStatus()));
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getContentType());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getByteSize());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(String.valueOf(urlElement.getDuration()));
                sb.append("</td>");
                sb.append("<td>");
                sb.append(TimeUtils.humanizeToDate(urlElement.getTimestamp()));
                sb.append("</td>");
                sb.append("<td>");
                sb.append(urlElement.getMethod());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(String.valueOf(urlElement.getRedirectCount()));
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

    public String createHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>\n" +
                "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"./VAADIN/themes/mytheme/xsltStyle.css?v=7.6.4\" />\n" +
                "\t</head><body>");

        sb.append("<div>");
        sb.append("<h1>Link Checking Statistics:</h1>");
        //general table
        List<String> columnNames = Arrays.asList("Status", "Count", "Average Response Duration(ms)", "Max Response Duration(ms)");
        List<List<Number>> rows = new ArrayList<>();

        AggregateIterable<Document> iterable = getStatusStatistics();
        for (Document doc : iterable) {
            List<Number> row = new ArrayList<>();

            row.add(doc.getInteger("_id"));
            row.add(doc.getInteger("count"));
            row.add(doc.getDouble("avg_resp"));
            row.add(doc.getLong("max_resp"));

            rows.add(row);
        }

        int total = 0;
        double avgResp = 0.0;
        iterable = getStatusStatisticsTotal();
        for (Document doc : iterable) {
            //there is only one document
            total = doc.getInteger("count");
            avgResp = doc.getDouble("avg_resp");
        }

        sb.append(createStatisticsTable("Overall", columnNames, rows, total, avgResp));

        sb.append("<h2>Collections:</h2>");

        //for each collection
        //each collection statistics
        try (Stream<Path> paths = Files.walk(Paths.get(Configuration.OUTPUT_DIRECTORY.toString() + "/collections/"))) {

            List<Path> files = paths.filter(Files::isRegularFile).collect(Collectors.toList());

            for (Path path : files) {

                rows = new ArrayList<>();

                String collectionName = path.getFileName().toString().split("\\.")[0];

                iterable = getStatusStatistics(collectionName);


                boolean empty = true;
                for (Document doc : iterable) {
                    if (empty) {
                        empty = false;
                    }

                    List<Number> row = new ArrayList<>();

                    row.add(doc.getInteger("_id"));
                    row.add(doc.getInteger("count"));
                    row.add(doc.getDouble("avg_resp"));
                    row.add(doc.getLong("max_resp"));

                    rows.add(row);
                }

                if (!empty) {
                    total = 0;
                    avgResp = 0.0;

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


                    sb.append(createStatisticsTable(collectionName, columnNames, rows, total, avgResp));
                }


            }

        } catch (IOException e) {
            _logger.error("Can't get the filenames list of collection reports.");
            //don't put those in the html
        }

        //links to be checked
        //this kind of doesnt make sense and it is already in the collection reports
//        iterable = handler.getLinksToBeCheckedStatistics();
//        for (Document doc : iterable) {
//            _logger.info(doc.toString());
//        }

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String createStatisticsTable(String collectionName, List<String> columnNames, List<List<Number>> rows, int total, double avgResp) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>" + collectionName + "</h3>");
        sb.append("<table>");
        sb.append("<thead>");
        sb.append("<tr>");
        for (String columnName : columnNames) {
            sb.append("<th>").append(columnName).append("</th>");
        }
        sb.append("</tr>");

        sb.append("</thead>");
        sb.append("<tbody>");

        for (List<Number> row : rows) {
            sb.append("<tr>");
            for (int i = 0; i < row.size(); i++) {
                sb.append("<td align='right'>");
                Number status = row.get(i);
                if (i == 0) {//first element is the status code
                    String element = "<a href='#!ResultView/statistics//" + collectionName + "/" + status + "'>" + numberFormatter.format(status) + "</a>";
                    sb.append(element);
                } else {
                    sb.append(numberFormatter.format(row.get(i)));
                }
                sb.append("</td>");

            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");
        sb.append("</table>");

        sb.append("<b>Total Count: </b>" + numberFormatter.format(total) + "<br>");
        sb.append("<b>Average Response Duration(ms): </b>" + numberFormatter.format(avgResp));

        return sb.toString();
    }

}
