package eu.clarin.helpers;

import eu.clarin.cmdi.curation.utils.CategoryColor;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;
import eu.clarin.main.Configuration;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LinkCheckerStatisticsHelper {

    public static String createURLTable(String collectionName, Category category) throws SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"creation-time\">" + TimeUtils.humanizeToDate(System.currentTimeMillis()) + "</div>");
        if(!"overall".equals(collectionName)) {
           sb.append("<div class=\"download\">download as zipped <a href=\"/download/xml/statistics/" + collectionName + "/" + category + "\">xml</a>");
           sb.append(" <a href=\"/download/json/statistics/" + collectionName + "/" + category + "\">json</a>");
           sb.append(" <a href=\"/download/tsv/statistics/" + collectionName + "/" + category + "\">tsv</a></div>");
        }
        sb.append("<div class=\"clear\" />");
        sb.append("<div>");
        sb.append("<h1>Link Checking Statistics (Category:" + category + "):</h1>");
        sb.append("<h3>").append(collectionName.replace("_", " ")).append(":</h3>"); 
        sb.append(  
              "(total: "  +  
              Configuration.checkedLinkResource.getCount(
                 Configuration.checkedLinkResource.getCheckedLinkFilter()
                    .setProviderGroupIs(collectionName)
                    .setCategoryIs(category)
                    .setIsActive(true)) +
              ")<br />"
           );

        List<String> columnNames = Arrays.asList("Url", "Status", "Info", "Record");

        sb.append("<table class='reportTable' id='statsTable' data-collection='" + collectionName + "' data-category='" + category + "'>");
        sb.append("<thead>");
        sb.append("<tr>");
        for (String columnName : columnNames) {
            sb.append("<th>").append(columnName).append("</th>");
        }
        sb.append("</tr>");

        sb.append("</thead>");
        sb.append("<tbody id='reportTableTbody'>");

        sb.append(getHtmlRowsInBatch(collectionName, category, 0));

        sb.append("</tbody>");
        sb.append("</table>");
        sb.append("<div id='tableEndSpan' class='centerContainer'></div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }

    public static String getHtmlRowsInBatch(String collectionName, Category category, int batchCount) throws SQLException {

        int start = batchCount * 100;
        int end = start + 100;

        StringBuilder sb = new StringBuilder();

        try (Stream<CheckedLink> links = Configuration.checkedLinkResource.get(
                 Configuration.checkedLinkResource.getCheckedLinkFilter()
                    .setProviderGroupIs(collectionName)
                    .setCategoryIs(category)
                    .setIsActive(true)
                    .setLimit(start, end)
                    )) {

            links.forEach(checkedLink -> {
                sb.append("<tr>");
                String url = checkedLink.getUrl();
                String urlWithBreak = url.replace("_", "_<wbr>");

                //url
                sb.append("<td style='background-color:" + CategoryColor.getColor(category) + "'>");
                sb.append("<a href='").append(url).append("'>").append(urlWithBreak).append("</a>");
                sb.append("</td>");

                //status
                sb.append("<td>");
                sb.append(checkedLink.getStatus() == null ? "N/A" : checkedLink.getStatus());
                sb.append("</td>");

                //info button
                sb.append("<td>");
                sb.append("<button class='showUrlInfo btn btn-info'>Show</button>");
                sb.append("</td>");

                //record
                sb.append("<td>");
                //some html css table too wide work around
                String record = checkedLink.getRecord();
                if (record != null) {
                    String recordWithBreaks = record.replace("_", "_<wbr>");

                    //todo maybe put these in properties file?
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

                String expectedContent = checkedLink.getExpectedMimeType() == null ? "Not Specified" : checkedLink.getExpectedMimeType();
                String content = checkedLink.getContentType() == null ? "N/A" : checkedLink.getContentType();
                sb.append("<b>Expected Content Type: </b>").append(expectedContent).append("<br>");
                sb.append("<b>Content Type: </b>").append(content).append("<br>");

                sb.append("<b>Byte Size: </b>").append(checkedLink.getByteSize() == null ? "N/A" : checkedLink.getByteSize()).append("<br>");
                sb.append("<b>Request Duration(ms): </b>").append(checkedLink.getDuration()).append("<br>");

                String method = checkedLink.getMethod() == null ? "N/A" : checkedLink.getMethod();
                sb.append("<b>Method: </b>").append(method).append("<br>");
                sb.append("<b>Timestamp: </b>").append(checkedLink.getCheckingDate());
                sb.append("</td>");
                //info end

                sb.append("</td></tr>");


            });

            return sb.toString();
        }
    }
}
