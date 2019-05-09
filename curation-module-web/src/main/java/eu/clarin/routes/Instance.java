package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HTMLHelpers.HtmlManipulator;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/instance")
public class Instance {

    private static final Logger _logger = Logger.getLogger(Instance.class);

    @GET
    @Path("/{instanceName}")
    public Response getInstance(@PathParam("instanceName") String instanceName) {

        String[] split = instanceName.split(".");
        if (split.length != 2) {
            return Response.status(400).entity("Instance name must end with either '.xml' or '.html'.").build();
        }
        String extension = split[1];
        String location;

        try {

            switch (extension) {
                case "xml":
                    location = Configuration.OUTPUT_DIRECTORY + "/xml/instances/";
                    String instanceXML = FileManager.readFile(location + instanceName);
                    return Response.ok().entity(instanceXML).type(MediaType.TEXT_XML).build();
                case "html":
                    location = Configuration.OUTPUT_DIRECTORY + "/html/instances/";
                    String instanceHTML = FileManager.readFile(location + instanceName);
                    return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(instanceHTML, null)).type(MediaType.TEXT_HTML).build();
                default:
                    return Response.status(400).entity("Instance name must end with either xml or html.").build();
            }
        } catch (IOException e) {
            _logger.error("There was an error reading the instance: " + instanceName);
            return Response.status(404).entity("The instance " + instanceName + " doesn't exist.").build();
        }

    }
}
