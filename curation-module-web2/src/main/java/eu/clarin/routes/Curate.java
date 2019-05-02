package eu.clarin.routes;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.ErrorReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.utils.FileNameEncoder;
import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HTMLHelpers.HtmlManipulator;
import eu.clarin.helpers.HTMLHelpers.NavbarButton;
import eu.clarin.helpers.XSLTTransformer;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Path("/")
public class Curate {

    private static final Logger _logger = Logger.getLogger(Curate.class);

    @GET
    @Path("/instances")
    public Response getInstances() {
        try {
            String instance = FileManager.readFile(Configuration.VIEW_RESOURCES_PATH + "/html/instance.html");

            return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(instance, null)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading instance.html: ", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/instance/{instanceName}")
    public Response getInstance(@PathParam("instanceName") String fileName) {
        String filePath = Configuration.OUTPUT_DIRECTORY + "/xml/instances/" + fileName;
        File instance = new File(filePath);

        if (Files.exists(instance.toPath())) {
            try {
                String result = FileManager.readFile(filePath);
                return Response.ok(result).type(MediaType.TEXT_XML).build();
            } catch (IOException e) {
                return Response.serverError().build();
            }
        } else {
            return Response.status(404).entity("The instance report with that name doesn't exist. Try curating it again.").type(MediaType.TEXT_PLAIN).build();
        }

    }

    //todo refactor this and next method to remove duplicate code
    @GET
    @Path("/curate/instance")
    public Response getInstanceQueryParam(@QueryParam("url-input") String url) {

        if (url == null || url.isEmpty()) {
            return Response.status(400).entity("Input URL can't be empty.").type(MediaType.TEXT_PLAIN).build();
        }
        String fileName = FileNameEncoder.encode(url);

        String htmlFilePath = Configuration.OUTPUT_DIRECTORY + "/html/instances/" + fileName + ".html";
        File instance = new File(htmlFilePath);

        //if html representation of report exists, return it
        if (Files.exists(instance.toPath())) {

            try {
                String result = FileManager.readFile(htmlFilePath);
                return Response.ok(HtmlManipulator.addContentToGenericHTML(result, null)).type(MediaType.TEXT_HTML).build();
            } catch (IOException e) {
                return Response.serverError().build();
            }
        } else {
            String reportPath = Configuration.OUTPUT_DIRECTORY + "/xml/instances/" + fileName + ".xml";

            //if xml representation of report exists, transform to html and return it
            if (!Files.exists(Paths.get(reportPath))) {

                Report r;
                try {
                    r = new CurationModule().processCMDInstance(new URL(url));
                } catch (MalformedURLException e) {
                    return Response.status(400).entity("Input URL is malformed. Make sure to put http or https in front.").type(MediaType.TEXT_PLAIN).build();
                } catch (Exception e) {
                    _logger.error("There was an exception processing the cmd instance: " + e.getMessage());
                    return Response.status(400).entity("There was a problem when processing the input. Please make sure that the URL points to a valid cmd file.").type(MediaType.TEXT_PLAIN).build();
                }

                try {

                    if (r instanceof ErrorReport) {
                        return Response.status(400).entity(((ErrorReport) r).error).type(MediaType.TEXT_PLAIN).build();
                    }

                    File file = new File(reportPath);
                    file.createNewFile();
                    file.setReadable(true, false);
                    r.toXML(Files.newOutputStream(Paths.get(reportPath)));
                } catch (IOException e) {
                    _logger.error("There was a problem generating the report: ", e);
                    return Response.serverError().build();
                }
            }

            try {
                String report = FileManager.readFile(reportPath);

                XSLTTransformer transformer = new XSLTTransformer();
                String result = transformer.transform(CurationEntity.CurationEntityType.INSTANCE, report);

                FileManager.writeToFile(htmlFilePath, result);

                return Response.ok(HtmlManipulator.addContentToGenericHTML(result, null)).type(MediaType.TEXT_HTML).build();
            } catch (IOException e) {
                _logger.error("There was a problem reading or writing to files: ", e);
                return Response.serverError().build();
            }


        }
    }


    //this is for drag and drop instance form
    @POST
    @Path("/curate/instance")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postInstance(@FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition fileMetaData) {


        String fileName = fileMetaData.getFileName();
        if (!fileName.endsWith(".xml")) {
            return Response.status(400).entity("The cmdi record must be an xml file.").type(MediaType.TEXT_PLAIN).build();
        }
        String htmlFilePath = Configuration.OUTPUT_DIRECTORY + "/html/instances/" + fileName.split("\\.")[0] + ".html";
        File instance = new File(htmlFilePath);

        //if html representation of report exists, return it
        if (Files.exists(instance.toPath())) {

            try {
                String result = FileManager.readFile(htmlFilePath);
                return Response.ok(HtmlManipulator.addContentToGenericHTML(result, null)).type(MediaType.TEXT_HTML).build();
            } catch (IOException e) {
                return Response.serverError().build();
            }
        } else {
            String reportPath = Configuration.OUTPUT_DIRECTORY + "/xml/instances/" + fileName; //filename already ends with ".xml"

            //if xml representation of report exists, transform to html and return it
            if (!Files.exists(Paths.get(reportPath))) {
                //read inputstream into string (from stackoverflow, like all code ever)
                String content = new BufferedReader(new InputStreamReader(fileInputStream))
                        .lines().collect(Collectors.joining("\n"));

                try {
                    String tempPath = System.getProperty("java.io.tmpdir") + "/" + fileName;
                    FileManager.writeToFile(tempPath, content);
                    Report r;
                    try {
                        r = new CurationModule().processCMDInstance(Paths.get(tempPath));
                    } catch (MalformedURLException e) {
                        return Response.status(400).entity("Input URL is malformed.").type(MediaType.TEXT_PLAIN).build();
                    } catch (Exception e) {
                        _logger.error("There was an exception processing the cmd instance: " + e.getMessage());
                        return Response.status(400).entity("There was a problem when processing the input. Please make sure to upload a valid cmd file.").type(MediaType.TEXT_PLAIN).build();
                    }

                    if (r instanceof ErrorReport) {
                        return Response.status(400).entity(((ErrorReport) r).error).type(MediaType.TEXT_PLAIN).build();
                    }

                    File file = new File(reportPath);
                    file.createNewFile();
                    file.setReadable(true, false);
                    r.toXML(Files.newOutputStream(Paths.get(reportPath)));
                } catch (IOException e) {
                    _logger.error("There was a problem generating the report: ", e);
                    return Response.serverError().build();
                }
            }

            try {
                String report = FileManager.readFile(reportPath);

                XSLTTransformer transformer = new XSLTTransformer();
                String result = transformer.transform(CurationEntity.CurationEntityType.INSTANCE, report);

                FileManager.writeToFile(htmlFilePath, result);

                return Response.ok(HtmlManipulator.addContentToGenericHTML(result, null)).type(MediaType.TEXT_HTML).build();
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

            List<NavbarButton> buttons = new ArrayList<>(Arrays.asList(new NavbarButton("/profiles/tsv", "Export as TSV")));
            return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(profiles, buttons)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading ProfilesReport.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/profiles/tsv")
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

    @GET
    @Path("/collections")
    public Response getCollections() {
        try {
            String collections = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/collections/CollectionsReport.html");

            List<NavbarButton> buttons = new ArrayList<>(Arrays.asList(new NavbarButton("/collections/tsv", "Export as TSV")));
            return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(collections, buttons)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading CollectionsReport.html: ", e);
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/collections/tsv")
    public Response getCollectionsTSV() {
        String collectionsTSVPath = Configuration.OUTPUT_DIRECTORY + "/tsv/collections/CollectionsReport.tsv";

        StreamingOutput fileStream = output -> {
            java.nio.file.Path path = Paths.get(collectionsTSVPath);
            byte[] data = Files.readAllBytes(path);
            output.write(data);
            output.flush();
        };

        return Response.ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = CollectionsReport.tsv").build();
    }


    @GET
    @Path("/statistics")
    public Response getStatistics() {
        try {
            String statistics = FileManager.readFile(Configuration.OUTPUT_DIRECTORY + "/html/statistics/LinkCheckerReport.html");

            return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(statistics, null)).type("text/html").build();
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

            return Response.ok().entity(HtmlManipulator.addContentToGenericHTML(help, null)).type("text/html").build();
        } catch (IOException e) {
            _logger.error("Error when reading help.html: ", e);
            return Response.serverError().build();
        }
    }


}
