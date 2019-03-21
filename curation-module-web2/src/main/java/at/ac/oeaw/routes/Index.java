package at.ac.oeaw.routes;

import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/index")
public class Index {

    private static final Logger _logger = Logger.getLogger(Index.class);

    @GET
    @Path("/")
    public Response getIndex(){

        _logger.info("woking?");
        return Response.ok("Here is configuration...").build();
    }
}
