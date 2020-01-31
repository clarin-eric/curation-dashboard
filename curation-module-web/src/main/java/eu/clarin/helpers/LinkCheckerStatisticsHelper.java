package eu.clarin.helpers;

import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.filters.impl.ACDHCheckedLinkFilter;
import eu.clarin.main.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

//this class creates a html report
public class LinkCheckerStatisticsHelper {
    private static final Logger _logger = LoggerFactory.getLogger(LinkCheckerStatisticsHelper.class);


//    private AggregateIterable<Document> getStatusStatistics() {
//
//        return linksChecked.aggregate(Arrays.asList(
//                Aggregates.group("$status",
//                        Accumulators.sum("count", 1),
//                        Accumulators.avg("avg_resp", "$duration"),
//                        Accumulators.max("max_resp", "$duration")
//                ),
//                Aggregates.sort(orderBy(ascending("_id")))
//        ));
//
//    }

//    private AggregateIterable<Document> getStatusStatistics(String collectionName) {
//        if (collectionName.equals("Overall")) {
//            return getStatusStatistics();
//        }
//
//        return linksChecked.aggregate(Arrays.asList(
//                Aggregates.match(eq("collection", collectionName)),
//                Aggregates.group("$status",
//                        Accumulators.sum("count", 1),
//                        Accumulators.avg("avg_resp", "$duration"),
//                        Accumulators.max("max_resp", "$duration")
//                ),
//                Aggregates.sort(orderBy(ascending("_id")))
//        ));
//    }

//    private AggregateIterable<Document> getStatusStatisticsTotal() {
//
//        return linksChecked.aggregate(Arrays.asList(
//                Aggregates.group("_id",
//                        Accumulators.sum("count", 1)
//                )
//        ));
//
//    }
//
//    private AggregateIterable<Document> getStatusStatisticsTotal(String collectionName) {
//        if (collectionName.equals("Overall")) {
//            return getStatusStatisticsTotal();
//        }
//
//        return linksChecked.aggregate(Arrays.asList(
//                Aggregates.match(eq("collection", collectionName)),
//                Aggregates.group("_id",
//                        Accumulators.sum("count", 1)
//                )
//        ));
//
//    }

//    //this method doesn't take 0 status codes into consideration. 0 means there was an error in the URL and there was no request sent.
//    private AggregateIterable<Document> getStatusStatisticsAvg() {
//
//        return linksChecked.aggregate(Arrays.asList(
//                Aggregates.match(not(eq("status", 0))),
//                Aggregates.group("_id",
//                        Accumulators.avg("avg_resp", "$duration")
//                )
//        ));
//    }
//
//
//    //this method doesn't take 0 status codes into consideration. 0 means there was an error in the URL and there was no request sent.
//    private AggregateIterable<Document> getStatusStatisticsAvg(String collectionName) {
//        if (collectionName.equals("Overall")) {
//            return getStatusStatisticsAvg();
//        }
//
//        return linksChecked.aggregate(Arrays.asList(
//                Aggregates.match(and(eq("collection", collectionName), not(eq("status", 0)))),
//                Aggregates.group("_id",
//                        Accumulators.avg("avg_resp", "$duration")
//                )
//        ));
//
//    }

    public static String createURLTable(String collectionName, int status) throws SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("<div>");
        sb.append("<h1>Link Checking Statistics (Status:" + status + "):</h1>");
        sb.append("<h3>").append(collectionName.replace("_", " ")).append(":</h3>");

        List<String> columnNames = Arrays.asList("Url", "Category", "Info", "Record");

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

    public static String getHtmlRowsInBatch(String collectionName, int status, int batchCount) throws SQLException {

        int start = batchCount * 100;
        int end = start + 100;

        StringBuilder sb = new StringBuilder();

        ACDHCheckedLinkFilter filter = new ACDHCheckedLinkFilter(collectionName, status);
        try(Stream<CheckedLink> links = Configuration.checkedLinkResource.get(Optional.of(filter), start, end)) {

            links.forEach(checkedLink -> {
                sb.append("<tr>");
                //todo move category into the database instead of checking it everywhere??
                //i cant really check the undetermined without a list
                String url = checkedLink.getUrl();
                String urlWithBreak = url.replace("_", "_<wbr>");
                //TODO use statuscodemapper.

                String category;
                if (status == 200) {
                    category = "Ok";
                    sb.append("<td style='background-color:#cbe7cc'>");
                    sb.append("<a href='").append(url).append("'>").append(urlWithBreak).append("</a>");
                    sb.append("</td>");
                    sb.append("<td style='background-color:#cbe7cc'>");
                    sb.append(category);
                    sb.append("</td>");
                } else if (status == 401 || status == 405 || status == 429) {
                    category = "Undetermined";
                    sb.append("<td style='background-color:#fff7b3'>");
                    sb.append("<a href='").append(url).append("'>").append(urlWithBreak).append("</a>");
                    sb.append("</td>");
                    sb.append("<td style='background-color:#fff7b3'>");
                    sb.append(category);
                    sb.append("</td>");
                } else {
                    category = "Broken";
                    sb.append("<td style='background-color:#f2a6a6'>");
                    sb.append("<a href='").append(url).append("'>").append(urlWithBreak).append("</a>");
                    sb.append("</td>");
                    sb.append("<td style='background-color:#f2a6a6'>");
                    sb.append(category);
                    sb.append("</td>");
                }

                //button
                sb.append("<td>");
                sb.append("<button class='showUrlInfo btn btn-info'>Show</button>");
                sb.append("</td>");

                sb.append("<td>");
                //some html css table too wide work around
                String record = checkedLink.getRecord();
                if (record != null) {
                    String recordWithBreaks = record.replace("_", "_<wbr>");

                    if (Configuration.clarinCollections.contains(collectionName)) {
                        String clarinURLPrefix = "https://vlo.clarin.eu/data/clarin/results/cmdi/";
                        sb.append("<a href='").append(clarinURLPrefix + collectionName + "/" + record + ".xml").append("'>").append(recordWithBreaks).append("</a>");
                    } else if (Configuration.europeanaCollections.contains(collectionName)) {
                        String europeanaURLPrefix = "https://vlo.clarin.eu/data/europeana/results/cmdi/";
                        sb.append("<a href='").append(europeanaURLPrefix + collectionName + "/" + record + ".xml").append("'>").append(recordWithBreaks).append("</a>");
                    } else {
                        sb.append(recordWithBreaks);
                    }
                }

                sb.append("</tr>");

                //info
                sb.append("<tr hidden><td colspan='4'>");
                String message = checkedLink.getMessage().replace("_", "_<wbr>");
                sb.append("<b>Message: </b> ").append(message).append("<br>");
                //because this field is new, older entries dont have it and it results in null, so a null check to make it more user friendly
                String expectedContent = checkedLink.getExpectedMimeType() == null ? "Not Specified" : checkedLink.getExpectedMimeType();
                String content = checkedLink.getContentType();

                sb.append("<b>Expected Content Type: </b>").append(expectedContent).append("<br>");
                sb.append("<b>Content Type: </b>").append(content).append("<br>");
                sb.append("<b>Byte Size: </b>").append(checkedLink.getByteSize()).append("<br>");
                sb.append("<b>Request Duration(ms): </b>").append(checkedLink.getDuration()).append("<br>");

                String method = checkedLink.getMethod() == null ? "N/A" : checkedLink.getMethod();
                sb.append("<b>Method: </b>").append(method).append("<br>");
                sb.append("<b>Timestamp: </b>").append(checkedLink.getTimestamp());
                sb.append("</td>");
                //info end

                sb.append("</td></tr>");


            });

            return sb.toString();
        }
    }
}
