package eu.clarin.routes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

@Path("/curate")
public class Curate {

    private static final Logger _logger = Logger.getLogger(Curate.class);

    //todo load correct path variables to config

    @GET
    @Path("/instances")
    public Response getInstances() {

        return null;
    }

    @GET
    @Path("/profiles")
    public Response getProfiles() {
        return null;
    }

    @GET
    @Path("/collections")
    public Response getCollections() {
        return null;
    }

    @GET
    @Path("/statistics")
    public Response getStatistics() {
        return null;
    }

    @GET
    @Path("/help")
    public Response getHelp() {
        return null;
    }


}
