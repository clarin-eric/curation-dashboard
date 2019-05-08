package eu.clarin.routes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import eu.clarin.helpers.FileManager;
import eu.clarin.main.Configuration;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
@Path("/rest")
public class Rest {
    private static final Logger _logger = Logger.getLogger(Rest.class);

    @GET
    @Path("/profile/{profileReportName}")
    public Response getProfile(@PathParam("profileReportName") String profileReportName) {
        String filePath = Configuration.OUTPUT_DIRECTORY + "/xml/profiles/" + profileReportName;
        File instance = new File(filePath);

        if (Files.exists(instance.toPath())) {
            try {
                String result = FileManager.readFile(filePath);
                return Response.ok(result).type(MediaType.TEXT_XML).build();
            }
            catch (IOException e) {
                return Response.serverError().build();
            }
        } else {
            return Response.status(404)
                    .entity("A profile report " + profileReportName + " doesn't exist.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

    }

    @GET
    @Path("/collection/{collectionReportName}")
    public Response getcollection(@PathParam("collectionReportName") String collectionReportName) {
        String filePath = Configuration.OUTPUT_DIRECTORY + "/xml/collections/" + collectionReportName;
        File instance = new File(filePath);

        if (Files.exists(instance.toPath())) {
            try {
                String result = FileManager.readFile(filePath);
                return Response.ok(result).type(MediaType.TEXT_XML).build();
            }
            catch (IOException e) {
                return Response.serverError().build();
            }
        } else {
            return Response.status(404).entity("A collection report " + collectionReportName + " doesn't exist.")
                    .type(MediaType.TEXT_PLAIN).build();
        }
    }
}
