package eu.clarin.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
@Produces({MediaType.TEXT_PLAIN,MediaType.APPLICATION_XML})
public class CurationRestService {

    private static final Logger _logger = LoggerFactory.getLogger(CurationRestService.class);

    @GET
    @Path("/instance/")
    public Response assessInstance(@QueryParam("url") String url) {
        _logger.info("curating " + url);
        try {
            return Response.ok(new CurationModule().processCMDInstance(new URL(url))).type(MediaType.APPLICATION_XML).build();
        } catch (MalformedURLException e) {
            return Response.status(400).entity("The url is malformed: " + url).type(MediaType.TEXT_PLAIN).build();
        } catch (IOException | InterruptedException e) {
            _logger.error("Error when processing instance from url: " + url + " . Message: " + e.getMessage());
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/profile/")
    public Response assesProfileByUrl(@QueryParam("url") String url) {
        _logger.info("curating profile " + url);
        try {
            return Response.ok(new CurationModule().processCMDProfile(new URL(url))).type(MediaType.APPLICATION_XML).build();
        } catch (InterruptedException e) {
            _logger.error("Error when processing profile from url: " + url + " . Message: " + e.getMessage());
            return Response.serverError().build();
        } catch (MalformedURLException e) {
            return Response.status(400).entity("The url is malformed: " + url).type(MediaType.TEXT_PLAIN).build();
        }
    }

    @GET
    @Path("/profile/id/{id}")
    public Response assesProfileById(@PathParam("id") String id) {
        _logger.info("curating profile " + id);
        try {
            return Response.ok(new CurationModule().processCMDProfile(id)).type(MediaType.APPLICATION_XML).build();
        } catch (InterruptedException e) {
            _logger.error("Error when processing profile from id: " + id + " . Message: " + e.getMessage());
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/collection/{collectionName}")
    public Response getCollectionReport(@PathParam("collectionName") String collectionName) {

        File collection = new File(Configuration.OUTPUT_DIRECTORY.toString() + "/collections/" + collectionName + ".xml");
        if (Files.exists(collection.toPath())) {
            return Response.ok(collection).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(404).type(MediaType.TEXT_PLAIN).entity("The collection with name: " + collectionName + " doesn't exist.").build();
        }

        //todo profiles
        //todo instances in temp folder

    }
}
