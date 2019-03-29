package eu.clarin.routes;

import eu.clarin.helpers.FileReader;
import eu.clarin.helpers.HtmlHelper;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/profile")
public class Profile {

    private static final Logger _logger = Logger.getLogger(Profile.class);

    @GET
    @Path("/{profileName}")
    public Response getProfile(@PathParam("profileName") String profileName){

        try{
            String profileHTML = FileReader.readFile(Configuration.OUTPUT_DIRECTORY+"/html/profiles/"+profileName);

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(profileHTML)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("There was an error reading the profile: "+profileName,e);
            return Response.status(404).entity("The profile "+profileName+ " doesn't exist.").build();
        }
    }
}
