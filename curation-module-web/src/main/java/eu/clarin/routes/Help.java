package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HTMLHelpers.HtmlManipulator;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/help")
public class Help {

    private static final Logger _logger = Logger.getLogger(Help.class);

    @GET
    @Path("/")
    public Response getHelp() {
        try {
            String help = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/help.html");

            return ResponseManager.returnHTML(200,help,null);
        } catch (IOException e) {
            _logger.error("Error when reading help.html: ", e);
            return ResponseManager.returnServerError();
        }
    }
}
