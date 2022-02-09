package eu.clarin.routes;


import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/view")
public class View {
   
    //fundament has these types of files
    private final List<String> textExtensions = Arrays.asList("css", "js", "svg");//svg is image but is saved as text
    private final List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png");

    private static Map<String, String> extensionMimeTypes = new HashMap<>() {
        /**
       * 
       */
      private static final long serialVersionUID = 1L;

      {
            put("css", "text/css");//css is not in mediatypes
            put("js", "text/javascript");//also js
            put("svg",MediaType.APPLICATION_SVG_XML);
            put("jpg","image/jpeg");
            put("jpeg","image/jpeg");
            put("png","image/png");
        }
    };


    @GET
    @Path("/{filepath : .+}")
    public Response handleView(@PathParam("filepath") String filePath) {
        try {

            String extension = filePath.split("\\.")[filePath.split("\\.").length - 1];

            if (textExtensions.contains(extension)) {
                String file = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + filePath);

                return ResponseManager.returnTextResponse(200,file,extensionMimeTypes.get(extension));

            } else if (imageExtensions.contains(extension)) {

                BufferedImage image = FileManager.readImage(Configuration.VIEW_RESOURCES_PATH + filePath);

                return ResponseManager.returnImageResponse(200,image,extension,extensionMimeTypes.get(extension));
            } else {
                return ResponseManager.returnError(404,"Requested file doesn't exist.");
            }

        } catch (IOException e) {
            return ResponseManager.returnError(404,"Requested file doesn't exist.");
        }

    }
}
