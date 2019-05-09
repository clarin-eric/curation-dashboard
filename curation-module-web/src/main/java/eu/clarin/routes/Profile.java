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

@Path("/profile")
public class Profile {

    private static final Logger _logger = Logger.getLogger(Profile.class);
    
    @GET
    @Path("/{profileName}")
    public Response getProfile(@PathParam("profileName") String profileName) {

        String[] split = profileName.split(".");
        if (split.length != 2) {
            return Response.status(400).entity("Profile name must end with either '.xml' or '.html'.").build();
        }
        String extension = split[1];

        String location;
        try {
            switch (extension) {
                case "xml":
                    location = Configuration.OUTPUT_DIRECTORY + "/xml/profiles/";
                    String profileXML = FileManager.readFile(location + profileName);
                    return Response.ok().entity(profileXML).type(MediaType.TEXT_XML).build();
                case "html":
                    location = Configuration.OUTPUT_DIRECTORY + "/html/profiles/";
                    String profileHTML = FileManager.readFile(location + profileName);
                    return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(profileHTML, null)).type(MediaType.TEXT_HTML).build();
                default:
                    return Response.status(400).entity("Profile name must end with either xml or html.").build();
            }
        } catch (IOException e) {
            _logger.error("There was an error reading the profile: " + profileName);
            return Response.status(404).entity("The profile " + profileName + " doesn't exist.").build();
        }
    }

    @GET
    @Path("/table")
    public Response getProfilesTable() {
        try {
            String profiles = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/profiles/ProfilesReport.html");

            return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(profiles, new NavbarButton("/profile/tsv", "Export as TSV"))).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading ProfilesReport.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/tsv")
    public Response getProfilesTSV() {
        String profilesTSVPath = Configuration.OUTPUT_DIRECTORY + "/tsv/profiles/ProfilesReport.tsv";

        StreamingOutput fileStream = output -> {
            java.nio.file.Path path = Paths.get(profilesTSVPath);
            byte[] data = Files.readAllBytes(path);
            output.write(data);
            output.flush();
        };

        return Response.ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = ProfilesReport.tsv").build();
    }


}
