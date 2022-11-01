package eu.clarin.controller;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.LinkCheckerStatisticsHelper;
import eu.clarin.helpers.ResponseManager;

import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/statistics")
public class Statistics {

    @GetMapping("/")
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

    @GetMapping("/{collectionName}/{category}")
    public Response getStatusStatsInit(@PathVariable("collectionName") String collectionName, @PathVariable("category") String categoryString) {
        log.info("URL category table requested for collection " + collectionName);
        String urlStatistics = null;

        try {
              Category category = Category.valueOf(categoryString);
              
              try {
                 urlStatistics = LinkCheckerStatisticsHelper.createURLTable(collectionName, category);
             } catch (SQLException e) {
                 log.error("Error in statistics", e);
                 return ResponseManager.returnServerError();
             }     
        }
        catch(Exception ex) {      
           log.error("Error in statistics", ex);
            return ResponseManager.returnError(400,"Given category doesn't match any of the following categories: "+ Arrays.toString(Category.values()));
        }

        return ResponseManager.returnHTML(200, urlStatistics);
    }


    @GetMapping("/{collectionName}/{category}/{batchCount}")
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
            log.error("Error in statistics", e);
            return ResponseManager.returnServerError();
        }
        return ResponseManager.returnTextResponse(200, urlBatchStatistics, "text/html");
    }

}
