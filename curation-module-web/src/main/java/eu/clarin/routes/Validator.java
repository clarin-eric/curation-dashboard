package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/")
public class Validator {

    private static final Logger _logger = Logger.getLogger(Validator.class);

    @Context
    HttpServletRequest request;

    @GET
    @Path("/")
    public Response getValidator() {
        try {
            String instance = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/validator.html");

            return ResponseManager.returnHTML(200, instance, null);
        } catch (IOException e) {
            _logger.error("Error when reading validator.html: ", e);
            return ResponseManager.returnServerError();
        }
    }

}
