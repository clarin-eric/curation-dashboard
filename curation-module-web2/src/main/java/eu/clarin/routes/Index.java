package eu.clarin.routes;

import eu.clarin.helpers.FileReader;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/")
public class Index {

    private static final Logger _logger = Logger.getLogger(Index.class);

    @GET
    @Path("/")
    public Response getIndex() {

        try {
            return Response.seeOther(new URI(Configuration.BASE_URL + "curate/instances")).build();
        } catch (URISyntaxException e) {
            _logger.error("There was an error with redirection: ", e);
            return Response.serverError().build();
        }

    }
}
