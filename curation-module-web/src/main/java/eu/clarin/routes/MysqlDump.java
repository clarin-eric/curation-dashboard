package eu.clarin.routes;

import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Path("/mysqlDump.gz")
public class MysqlDump {

    private static final Logger _logger = Logger.getLogger(MysqlDump.class);

    @GET
    @Path("/")
    public Response getDump(){

        try {
            String dumpPath = Configuration.OUTPUT_DIRECTORY+"/dump/status.sql.gz";
            final InputStream fileInStream = new FileInputStream(dumpPath);

            return ResponseManager.returnFile(200,fileInStream,"application/gzip","mysqlDump.gz");

        } catch (FileNotFoundException e) {
            _logger.error("There was an error getting the mysql dump file: ",e);
            return ResponseManager.returnServerError();
        }

    }

}
