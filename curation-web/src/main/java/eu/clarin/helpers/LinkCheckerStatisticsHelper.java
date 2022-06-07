package eu.clarin.helpers;

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
      if (!"overall".equals(collectionName)) {
         sb.append("<div class=\"download\">download as zipped <a href=\"/download/xml/statistics/" + collectionName
               + "/" + category + "\">xml</a>");
         sb.append(" <a href=\"/download/json/statistics/" + collectionName + "/" + category + "\">json</a>");
         sb.append(" <a href=\"/download/tsv/statistics/" + collectionName + "/" + category + "\">tsv</a></div>");
      }
      sb.append("<div class=\"clear\" />");
      sb.append("<div>");
      sb.append("<h1>Link Checking Statistics (Category:" + category + "):</h1>");
      sb.append("<h3>").append(collectionName.replace("_", " ")).append(":</h3>");
      sb.append("(total: " + Configuration.checkedLinkResource.getCount(Configuration.checkedLinkResource
            .getCheckedLinkFilter().setProviderGroupIs(collectionName).setCategoryIs(category).setIsActive(true))
            + ")<br />");

      List<String> columnNames = Arrays.asList("Url", "Status", "Info");

      sb.append("<table class='reportTable' id='statsTable' data-collection='" + collectionName + "' data-category='"
            + category + "'>");
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

   public static String getHtmlRowsInBatch(String collectionName, Category category, int batchCount)
         throws SQLException {

      int start = batchCount * 100;
      int end = start + 100;

      StringBuilder sb = new StringBuilder();

      try (Stream<CheckedLink> links = Configuration.checkedLinkResource
            .get(Configuration.checkedLinkResource.getCheckedLinkFilter().setProviderGroupIs(collectionName)
                  .setCategoryIs(category).setIsActive(true).setLimit(start, end))) {

         links.forEach(checkedLink -> {
            sb.append("<tr>");
            String url = checkedLink.getUrl();
            String urlWithBreak = url.replace("_", "_<wbr>");

            // url
            sb.append("<td class='" + category.name() + "'>");
            sb.append("<a href='").append(url).append("'>").append(urlWithBreak).append("</a>");
            sb.append("</td>");

            // status
            sb.append("<td>");
            sb.append(checkedLink.getStatus() == null ? "N/A" : checkedLink.getStatus());
            sb.append("</td>");

            // info button
            sb.append("<td>");
            sb.append("<button class='showUrlInfo btn btn-info'>Show</button>");
            sb.append("</td>");

            sb.append("</tr>");

            // info
            sb.append("<tr hidden><td colspan='3'>");

            sb.append("<b>Message: </b> ")
               .append(checkedLink.getMessage() == null? "N/A" : checkedLink.getMessage().replace("_", "_<wbr>")).append("<br>");

            sb.append("<b>Byte Size: </b>")
               .append(checkedLink.getByteSize() == null ? "N/A" : checkedLink.getByteSize()).append("<br>");
            
            sb.append("<b>Request Duration(ms): </b>")
               .append(checkedLink.getDuration() == null? "N/A":checkedLink.getDuration()).append("<br>");

            sb.append("<b>Method: </b>")
               .append(checkedLink.getMethod() == null ? "N/A" : checkedLink.getMethod()).append("<br>");
            
            // null check not necessary since checkingDate is non nullable
            sb.append("<b>Timestamp: </b>").append(checkedLink.getCheckingDate());
            
            sb.append("<b>Collection: </b>")
            .append(checkedLink.getProviderGroup() == null ? "N/A" : checkedLink.getProviderGroup()).append("<br>");
            sb.append("</td>");
            
            sb.append("<b>Record: </b>")
            .append(checkedLink.getRecord() == null ? "N/A" : checkedLink.getRecord()).append("<br>");
            sb.append("</td>");
            
            sb.append("<b>Expected mime-type: </b>")
            .append(checkedLink.getExpectedMimeType() == null ? "N/A" : checkedLink.getExpectedMimeType()).append("<br>");
            sb.append("</td>");
            
            // info end

            sb.append("</td></tr>");

         });

         return sb.toString();
      }
   }
}
