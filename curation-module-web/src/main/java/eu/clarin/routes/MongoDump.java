package eu.clarin.routes;

import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Path("/mongoDump.gz")
public class MongoDump {

    private static final Logger _logger = Logger.getLogger(MongoDump.class);

    @GET
    @Path("/")
    public Response getDump(){

        try {
            String dumpPath = Configuration.OUTPUT_DIRECTORY+"/dump/linkchecker.gz";
            final InputStream fileInStream = new FileInputStream(dumpPath);
            return Response.ok(fileInStream, "application/gzip").build();
        } catch (FileNotFoundException e) {
            _logger.error("There was an error getting the mongo dump file: ",e);
            return Response.serverError().build();
        }

    }
}
