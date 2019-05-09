package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HTMLHelpers.NavbarButton;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Path("/collection")
public class Collection {

    private static final Logger _logger = Logger.getLogger(Collection.class);

    @GET
    @Path("/{collectionName}")
    public Response getCollection(@PathParam("collectionName") String collectionName) {

        String[] split = collectionName.split("\\.");
        if (split.length != 2) {
            return ResponseManager.returnError(400, "Collection name must end with either xml or html.");
        }
        String extension = split[1];

        String location;
        try {
            switch (extension) {
                case "xml":
                    location = Configuration.OUTPUT_DIRECTORY + "/xml/collections/";
                    String collectionXML = FileManager.readFile(location + collectionName);
                    return ResponseManager.returnResponse(200, collectionXML, MediaType.TEXT_XML);
                case "html":
                    location = Configuration.OUTPUT_DIRECTORY + "/html/collections/";
                    String collectionHTML = FileManager.readFile(location + collectionName);
                    return ResponseManager.returnHTML(200, collectionHTML, null);
                default:
                    return ResponseManager.returnError(400, "Collection name must end with either xml or html.");
            }
        } catch (IOException e) {
            _logger.error("There was an error reading the collection: " + collectionName);
            return ResponseManager.returnError(404, "The collection " + collectionName + " doesn't exist.");
        }
    }

    @GET
    @Path("/table")
    public Response getCollectionsTable() {
        try {
            String collections = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/collections/CollectionsReport.html");

            return ResponseManager.returnHTML(200, collections, new NavbarButton("/collection/tsv", "Export as TSV"));
        } catch (IOException e) {
            _logger.error("Error when reading CollectionsReport.html: ", e);
            return ResponseManager.returnServerError();
        }

    }

    @GET
    @Path("/tsv")
    public Response getCollectionsTSV() {
        String collectionsTSVPath = Configuration.OUTPUT_DIRECTORY + "/tsv/collections/CollectionsReport.tsv";

        final InputStream fileInStream;
        try {
            fileInStream = new FileInputStream(collectionsTSVPath);
            return ResponseManager.returnFile(200, fileInStream, "text/tab-separated-values", "CollectionsReport.tsv");
        } catch (FileNotFoundException e) {
            _logger.error("There was an error getting the collectionsReport.tsv file: ", e);
            return ResponseManager.returnServerError();
        }

    }
}
