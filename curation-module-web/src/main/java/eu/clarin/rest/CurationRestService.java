package eu.clarin.rest;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

@Path("/")
@Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
public class CurationRestService {

    static final Logger _logger = LoggerFactory.getLogger(CurationRestService.class);

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
        _logger.info("Curating profile by url: " + url);
        try {
            return Response.ok(new CurationModule().processCMDProfile(new URL(url))).type(MediaType.APPLICATION_XML).build();
        } catch (InterruptedException e) {
            _logger.error("Error when processing profile from url: " + url + " . Message: " + e.getMessage());
            return Response.serverError().build();
        } catch (MalformedURLException e) {
            return Response.status(400).entity("This url is malformed: " + url).type(MediaType.TEXT_PLAIN).build();
        }
    }

    @GET
    @Path("/profile/{id}")
    public Response assesProfileById(@PathParam("id") String id) {

        String profileName = id.endsWith(".xml") ? id : id + ".xml";
        File profile = new File(Configuration.OUTPUT_DIRECTORY.toString() + "/profiles/" + profileName);
        if (Files.exists(profile.toPath())) {
            return Response.ok(profile).type(MediaType.APPLICATION_XML).build();
        } else {
            _logger.info("Curating profile by id: " + id);
            try {
                return Response.ok(new CurationModule().processCMDProfile(id)).type(MediaType.APPLICATION_XML).build();
            } catch (InterruptedException e) {
                _logger.error("Error when processing profile from id: " + id + " . Message: " + e.getMessage());
                return Response.serverError().build();
            }
        }
    }

    @GET
    @Path("/collection/{collectionName}")
    public Response getCollectionReport(@PathParam("collectionName") String collectionName) {

        collectionName = collectionName.endsWith(".xml") ? collectionName : collectionName + ".xml";
        File collection = new File(Configuration.OUTPUT_DIRECTORY.toString() + "/collections/" + collectionName);
        if (Files.exists(collection.toPath())) {
            return Response.ok(collection).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(404).type(MediaType.TEXT_PLAIN).entity("The collection with name: " + collectionName + " doesn't exist.").build();
        }

    }

    @GET
    @Path("/instance/{instanceName}")
    public Response getInstanceReport(@PathParam("instanceName") String instanceName) {

        instanceName = instanceName.endsWith(".xml") ? instanceName : instanceName + ".xml";
        File instance = new File(Configuration.OUTPUT_DIRECTORY.toString() + "/instances/" + instanceName);
        if (Files.exists(instance.toPath())) {
            return Response.ok(instance).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(404).type(MediaType.TEXT_PLAIN).entity("The instance with name: " + instanceName + " doesn't exist.").build();
        }

    }

    //todo then add these urls to reports and xslt
}
