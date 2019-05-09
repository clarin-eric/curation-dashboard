package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.ResponseManager;
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

        String[] split = instanceName.split("\\.");
        if (split.length != 2) {
            return ResponseManager.returnError(400, "Instance name must end with either '.xml' or '.html'.");
        }
        String extension = split[1];
        String location;

        try {

            switch (extension) {
                case "xml":
                    location = Configuration.OUTPUT_DIRECTORY + "/xml/instances/";
                    String instanceXML = FileManager.readFile(location + instanceName);
                    return ResponseManager.returnResponse(200, instanceXML, MediaType.TEXT_XML);
                case "html":
                    location = Configuration.OUTPUT_DIRECTORY + "/html/instances/";
                    String instanceHTML = FileManager.readFile(location + instanceName);
                    return ResponseManager.returnHTML(200, instanceHTML, null);
                default:
                    return ResponseManager.returnError(400, "Instance name must end with either '.xml' or '.html'.");
            }
        } catch (IOException e) {
            _logger.error("There was an error reading the instance: " + instanceName);
            return ResponseManager.returnError(404, "The instance " + instanceName + " doesn't exist.");
        }

    }
}
