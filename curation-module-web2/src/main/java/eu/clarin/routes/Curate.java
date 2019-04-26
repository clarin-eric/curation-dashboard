package eu.clarin.routes;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HtmlHelper;
import eu.clarin.helpers.XSLTTransformer;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;


@Path("/")
public class Curate {

    private static final Logger _logger = Logger.getLogger(Curate.class);

    @GET
    @Path("/instances")
    public Response getInstances() {
        try {
            String instance = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/instance.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(instance)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading instance.html: ", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/instance/{instanceName}")
    public Response getInstance(@PathParam("instanceName") String fileName) {
        String filePath = Configuration.OUTPUT_DIRECTORY + "/html/instances/" + fileName;
        File instance = new File(filePath);

        if (Files.exists(instance.toPath())) {
            try {
                String result = FileManager.readFile(filePath);
                return Response.ok(HtmlHelper.addContentToGenericHTML(result)).type(MediaType.TEXT_HTML).build();
            } catch (IOException e) {
                return Response.serverError().build();
            }
        } else {
            return Response.status(404).entity("The instance report with that name doesn't exist. Try curating it again.").type(MediaType.TEXT_PLAIN).build();
        }

    }

    //this is for drag and drop instance form
    @POST
    @Path("/curate/instance")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postInstances(@FormDataParam("file") InputStream fileInputStream,
                                  @FormDataParam("file") FormDataContentDisposition fileMetaData) {

        String fileName = fileMetaData.getFileName();
        String htmlFilePath = Configuration.OUTPUT_DIRECTORY + "/html/instances/" + fileName.split("\\.")[0] + ".html";
        File instance = new File(htmlFilePath);

        if (Files.exists(instance.toPath())) {

            try {
                String result = FileManager.readFile(htmlFilePath);
                return Response.ok(HtmlHelper.addContentToGenericHTML(result)).type(MediaType.TEXT_HTML).build();
            } catch (IOException e) {
                return Response.serverError().build();
            }
        } else {

            //read inputstream into string (from stackoverflow, like all code ever)
            String content = new BufferedReader(new InputStreamReader(fileInputStream))
                    .lines().collect(Collectors.joining("\n"));

            try {
                String tempPath = System.getProperty("java.io.tmpdir") + "/" + fileName;
                FileManager.writeToFile(tempPath, content);

                CMDInstanceReport r = (CMDInstanceReport) new CurationModule().processCMDInstance(Paths.get(tempPath));

                String reportPath = Configuration.OUTPUT_DIRECTORY + "/xml/instances/" + fileName.split("\\.")[0] + ".xml";
                r.toXML(Files.newOutputStream(Paths.get(reportPath)));

                String report = FileManager.readFile(reportPath);

                XSLTTransformer transformer = new XSLTTransformer();
                String result = transformer.transform(CurationEntity.CurationEntityType.INSTANCE, report);

                FileManager.writeToFile(htmlFilePath, result);

                return Response.ok(HtmlHelper.addContentToGenericHTML(result)).type(MediaType.TEXT_HTML).build();
            } catch (IOException e) {
                _logger.error("There was a problem reading or writing to files: ", e);
                return Response.serverError().build();
            }


        }
    }

    @GET
    @Path("/profiles")
    public Response getProfiles() {
        try {
            String profiles = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/profiles/ProfilesReport.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(profiles)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading ProfilesReport.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/collections")
    public Response getCollections() {
        try {
            String collections = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/collections/CollectionsReport.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(collections)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading CollectionsReport.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/statistics")
    public Response getStatistics() {
        try {
            String statistics = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/statistics/LinkCheckerReport.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(statistics)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading linkCheckerStatistics.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/help")
    public Response getHelp() {
        try {
            String help = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/help.html");

            return Response.ok().entity(HtmlHelper.addContentToGenericHTML(help)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading help.html: ", e);
            return Response.serverError().build();
        }
    }


}
