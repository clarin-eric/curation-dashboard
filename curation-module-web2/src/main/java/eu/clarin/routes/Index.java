package eu.clarin.routes;

import eu.clarin.helpers.FileReader;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/")
public class Index {

    private static final Logger _logger = Logger.getLogger(Index.class);

    @GET
    @Path("/")
    public Response getIndex() {

        try {
            String index = FileReader.readFile(Configuration.VIEW_RESOURCES_PATH + "html/generic.html");

            return Response.ok(index).type(MediaType.TEXT_HTML).build();
        } catch (IOException e) {
            _logger.error("Can't find generic.html",e);
            return Response.serverError().build();
        }

    }
}
