package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Slf4j
@Path("/help")
public class Help {

    @GET
    @Path("/")
    public Response getHelp() {
        try {
            String help = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/help.html");

            return ResponseManager.returnHTML(200, help);
        } catch (IOException e) {
            log.error("Error when reading help.html: ", e);
            return ResponseManager.returnServerError();
        }
    }
}
