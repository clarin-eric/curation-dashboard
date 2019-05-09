package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HTMLHelpers.HtmlManipulator;
import eu.clarin.helpers.HTMLHelpers.NavbarButton;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/collection")
public class Collection {

    private static final Logger _logger = Logger.getLogger(Collection.class);

    @GET
    @Path("/{collectionName}")
    public Response getCollection(@PathParam("collectionName") String collectionName) {

        String[] split = collectionName.split(".");
        if (split.length != 2) {
            return Response.status(400).entity("Collection name must end with either '.xml' or '.html'.").build();
        }
        String extension = split[1];

        String location;
        try {
            switch (extension) {
                case "xml":
                    location = Configuration.OUTPUT_DIRECTORY + "/xml/collections/";
                    String collectionXML = FileManager.readFile(location + collectionName);
                    return Response.ok().entity(collectionXML).type(MediaType.TEXT_XML).build();
                case "html":
                    location = Configuration.OUTPUT_DIRECTORY + "/html/collections/";
                    String collectionHTML = FileManager.readFile(location + collectionName);
                    return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(collectionHTML, null)).type(MediaType.TEXT_HTML).build();
                default:
                    return Response.status(400).entity("Collection name must end with either xml or html.").build();
            }
        } catch (IOException e) {
            _logger.error("There was an error reading the collection: " + collectionName);
            return Response.status(404).entity("The collection " + collectionName + " doesn't exist.").build();
        }
    }

    @GET
    @Path("/table")
    public Response getCollectionsTable() {
        try {
            String collections = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/collections/CollectionsReport.html");

            return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(collections, new NavbarButton("/collection/tsv", "Export as TSV"))).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading CollectionsReport.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/tsv")
    public Response getCollectionsTSV() {
        String collectionsTSVPath = Configuration.OUTPUT_DIRECTORY + "/tsv/collections/CollectionsReport.tsv";

        StreamingOutput fileStream = output -> {
            java.nio.file.Path path = Paths.get(collectionsTSVPath);
            byte[] data = Files.readAllBytes(path);
            output.write(data);
            output.flush();
        };

        return Response.ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = CollectionsReport.tsv").build();
    }
}
