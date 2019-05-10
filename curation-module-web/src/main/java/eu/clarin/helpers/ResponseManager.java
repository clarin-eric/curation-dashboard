package eu.clarin.helpers;

import eu.clarin.helpers.HTMLHelpers.HtmlManipulator;
import eu.clarin.helpers.HTMLHelpers.NavbarButton;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public final class ResponseManager {

    private static final Logger _logger = Logger.getLogger(ResponseManager.class);

    public static Response returnFile(int status, InputStream fileStream, String type, String fileName) {
        return Response.status(status).entity(fileStream).type(type).header("content-disposition", "attachment; filename = " + fileName).build();
    }

    public static Response returnResponse(int status, Object entity, String mediaType) {
        return Response.status(status).entity(entity).type(mediaType).build();
    }

    public static Response returnHTML(int status, String message, NavbarButton button) {

        try {
            return Response.status(status).entity(HtmlManipulator.addContentToGenericHTML(message, button)).type(MediaType.TEXT_HTML).build();
        } catch (IOException e) {
            _logger.error("Error reading generic.html");
            return returnServerError();
        }
    }

    public static Response returnError(int status, String message) {

        String error = "<h2>There was an error:</h2>";
        return returnHTML(status, error + message, null);
    }

    public static Response returnServerError() {
        return returnError(500, "There was a server error. Please report it on <a href='https://github.com/clarin-eric/clarin-curation-module/issues'>github</a>.");
    }

    public static Response redirect(String resultURL) {
        return Response.seeOther(URI.create(resultURL)).build();
    }
}
