package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HTMLHelpers.HtmlManipulator;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/")
public class Validator {

    private static final Logger _logger = Logger.getLogger(Validator.class);

    @GET
    @Path("/")
    public Response getValidator() {
        try {
            String instance = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/validator.html");

            return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(instance, null)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading validator.html: ", e);
            return Response.serverError().build();
        }
    }

}
