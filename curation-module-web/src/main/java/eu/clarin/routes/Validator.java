package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Slf4j
@Path("/")
public class Validator {

    @Context
    HttpServletRequest request;

    @GET
    @Path("/")
    public Response getValidator() {
        try {
            String instance = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/validator.html");

            return ResponseManager.returnHTML(200, instance);
        } catch (IOException e) {
            log.error("Error when reading validator.html: ", e);
            return ResponseManager.returnServerError();
        }
    }

}
