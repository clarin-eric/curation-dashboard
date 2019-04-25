package eu.clarin.routes;

import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HtmlHelper;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import java.io.IOException;

@Path("/collection")
public class Collection {

    private static final Logger _logger = Logger.getLogger(Collection.class);

    @GET
    @Path("/{collectionName}")
    public Response getCollection(@PathParam("collectionName") String collectionName){

        try{
            String collectionHTML = FileManager.readFile(Configuration.OUTPUT_DIRECTORY+"/html/collections/"+collectionName);

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(collectionHTML)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("There was an error reading the collection: "+collectionName);
            return Response.status(404).entity("The collection "+collectionName+ " doesn't exist.").build();
        }
    }
}
