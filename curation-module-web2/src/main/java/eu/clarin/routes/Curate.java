package eu.clarin.routes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.clarin.helpers.FileReader;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

@Path("/curate")
public class Curate {

    private static final Logger _logger = Logger.getLogger(Curate.class);

    //todo load correct path variables to config

    @GET
    @Path("/instances")
    public Response getInstances() {

        return null;
    }

    @GET
    @Path("/profiles")
    public Response getProfiles() {
        try {
            String profiles = FileReader.readFile(Configuration.OUTPUT_DIRECTORY + "/html/profiles/ProfilesReport.html");

            Document doc = Configuration.GENERIC_HTML_DOC;

            Element content = doc.getElementById("content");
            content.append(profiles);

            return Response.ok().entity(doc.html()).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading ProfilesReport.html: " + e.getMessage());
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/collections")
    public Response getCollections() {
        return null;
    }

    @GET
    @Path("/statistics")
    public Response getStatistics() {
        return null;
    }

    @GET
    @Path("/help")
    public Response getHelp() {
        return null;
    }


}
