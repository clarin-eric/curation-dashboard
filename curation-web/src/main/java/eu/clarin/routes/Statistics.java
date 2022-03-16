package eu.clarin.routes;


import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;
import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.LinkCheckerStatisticsHelper;
import eu.clarin.helpers.ResponseManager;

import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

@Slf4j
@Path("/statistics")
public class Statistics {

    @GET
    @Path("/")
    public Response getStatistics() {
        try {
            log.info("Statistics report requested.");
            String statistics = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/statistics/LinkCheckerReport.html");

            return ResponseManager.returnHTML(200, statistics);
        } catch (IOException e) {
            log.error("Error when reading linkCheckerStatistics.html: ", e);
            return ResponseManager.returnServerError();
        }
    }

    @GET
    @Path("/{collectionName}/{category}")
    public Response getStatusStatsInit(@PathParam("collectionName") String collectionName, @PathParam("category") String categoryString) {
        log.info("URL category table requested for collection " + collectionName);
        String urlStatistics = null;

        try {
              Category category = Category.valueOf(categoryString);
              
              try {
                 urlStatistics = LinkCheckerStatisticsHelper.createURLTable(collectionName, category);
             } catch (SQLException e) {
                 log.error("Error in statistics: "+e.getMessage());
                 return ResponseManager.returnServerError();
             }     
        }
        catch(Exception ex) {      

            return ResponseManager.returnError(400,"Given category doesn't match any of the following categories: "+ Arrays.toString(Category.values()));
        }

        return ResponseManager.returnHTML(200, urlStatistics);
    }


    @GET
    @Path("/{collectionName}/{category}/{batchCount}")
    public Response getStatusStats(@PathParam("collectionName") String collectionName, @PathParam("category") String category, @PathParam("batchCount") int batchCount) {
        log.info("URL batch requested with count " + batchCount + " for collection " + collectionName);
        String urlBatchStatistics = null;

        Category categoryEnum = Arrays.stream(Category.values())
                .filter(c -> c.name().equalsIgnoreCase(category)).findAny().orElse(null);

        if(categoryEnum==null){
            return ResponseManager.returnError(400,"Given category doesn't match any of the following categories: "+ Arrays.toString(Category.values()));
        }

        try {
            urlBatchStatistics = LinkCheckerStatisticsHelper.getHtmlRowsInBatch(collectionName, categoryEnum, batchCount);
        } catch (SQLException e) {
            log.error("Error in statistics: "+e.getMessage());
            return ResponseManager.returnServerError();
        }
        return ResponseManager.returnTextResponse(200, urlBatchStatistics, "text/html");
    }

}
