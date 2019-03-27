package eu.clarin.routes;


import eu.clarin.helpers.FileReader;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Path("/view")
public class View {

    private static final Logger logger = Logger.getLogger(View.class);
    //fundament has these types of files
    private final List<String> extensionsText = Arrays.asList("css", "js", "svg");
    private final List<String> extensionsImage = Arrays.asList("jpg", "png");


    @GET
    @Path("/{filepath : .+}")
    public Response handleView(@PathParam("filepath") String filePath) {
        try {

            String extension = filePath.split("\\.")[filePath.split("\\.").length - 1];

            if (extensionsText.contains(extension)) {
                String file = FileReader.readFile(Configuration.resourcesPath + filePath);
                return Response.status(200).entity(file).build();
            } else if (extensionsImage.contains(extension)) {
                BufferedImage image = FileReader.readImage(Configuration.resourcesPath + filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, extension, baos);
                byte[] imageData = baos.toByteArray();

                return Response.status(200).type("image/" + extension).entity(new ByteArrayInputStream(imageData)).build();
            } else {
                return Response.status(404).entity("Requested file doesn't exist.").build();
            }

        } catch (IOException e) {
            return Response.status(404).entity("Requested file doesn't exist.").build();
        }

    }
}
