package eu.clarin.routes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.clarin.helpers.FileReader;
import eu.clarin.helpers.HtmlHelper;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;


import java.io.IOException;



@Path("/")
public class Curate {

    private static final Logger _logger = Logger.getLogger(Curate.class);

    @GET
    @Path("/instances")
    public Response getInstances() {
        try {
            String instance = FileReader.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/instance.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(instance)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading instance.html: ", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/profiles")
    public Response getProfiles() {
        try {
            String profiles = FileReader.readFile(Configuration.OUTPUT_DIRECTORY + "/html/profiles/ProfilesReport.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(profiles)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading ProfilesReport.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/collections")
    public Response getCollections() {
        try {
            String collections = FileReader.readFile(Configuration.OUTPUT_DIRECTORY + "/html/collections/CollectionsReport.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(collections)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading CollectionsReport.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/statistics")
    public Response getStatistics() {
        try {
            String statistics = FileReader.readFile(Configuration.OUTPUT_DIRECTORY + "/html/statistics/LinkCheckerReport.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(statistics)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading linkCheckerStatistics.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/help")
    public Response getHelp() {
        try {
            String help = FileReader.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/help.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(help)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading help.html: ", e);
            return Response.serverError().build();
        }
    }


}
