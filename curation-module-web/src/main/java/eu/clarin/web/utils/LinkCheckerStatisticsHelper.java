package eu.clarin.web.utils;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import eu.clarin.cmdi.curation.main.Configuration;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

//this class connects with mongodb and creates a html report out of the queries
public class LinkCheckerStatisticsHelper {
    private static final Logger _logger = LoggerFactory.getLogger(LinkCheckerStatisticsHelper.class);

    private static final MongoClient mongoClient;
    private MongoCollection<Document> linksToBeChecked;
    MongoCollection<Document> linksChecked;

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

    public String createHTML() {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head>\n" +
                "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"./VAADIN/themes/mytheme/xsltStyle.css?v=7.6.4\" />\n" +
                "\t</head><body>");


        sb.append("<div>");
        sb.append("<h1>General Statistics:</h1>");
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
        sb.append(createTable(columnNames, rows));

        sb.append("<h2>Collections:</h2>");

        //for each collection
        //each collection statistics
        try (Stream<Path> paths = Files.walk(Paths.get(Configuration.OUTPUT_DIRECTORY.toString() + "/collections/"))) {

            List<Path> files = paths.filter(Files::isRegularFile).collect(Collectors.toList());

            for (Path path : files) {

                rows = new ArrayList<>();

                String collectionName = path.getFileName().toString().split("\\.")[0];
                sb.append("<h3>" + collectionName + ":</h3>");
                iterable = getStatusStatistics(collectionName);
                for (Document doc : iterable) {
                    List<Number> row = new ArrayList<>();

                    row.add(doc.getInteger("_id"));
                    row.add(doc.getInteger("count"));
                    row.add(doc.getDouble("avg_resp"));
                    row.add(doc.getLong("max_resp"));

                    rows.add(row);
                }
                sb.append(createTable(columnNames, rows));


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

    private String createTable(List<String> columnNames, List<List<Number>> rows) {
        StringBuilder sb = new StringBuilder();
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
            for (Number rowElement : row) {
                sb.append("<td>").append(rowElement).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");
        sb.append("</table>");


        return sb.toString();
    }

}
