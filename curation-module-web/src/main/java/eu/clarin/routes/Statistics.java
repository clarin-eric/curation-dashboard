package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HTMLHelpers.HtmlManipulator;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/statistics")
public class Statistics {

    private static final Logger _logger = Logger.getLogger(Statistics.class);

    @GET
    @Path("/")
    public Response getStatistics() {
        try {
            String statistics = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/statistics/LinkCheckerReport.html");

            return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(statistics, null)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading linkCheckerStatistics.html: ", e);
            return Response.serverError().build();
        }
    }

    //todo here statistics dynamic methods
}
