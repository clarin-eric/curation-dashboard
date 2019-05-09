package eu.clarin.routes;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.main.Main;
import eu.clarin.cmdi.curation.report.ErrorReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.utils.FileNameEncoder;
import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HTMLHelpers.HtmlManipulator;
import eu.clarin.helpers.XSLTTransformer;
import eu.clarin.main.Configuration;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;


@Path("/curate")
public class Curate {

    private static final Logger _logger = Logger.getLogger(Curate.class);

    //todo refactor this and next method to remove duplicate code
    @GET
    @Path("/")
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
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postInstance(@FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition fileMetaData) {


        String uploadFileName = fileMetaData.getFileName();
        if (!uploadFileName.endsWith(".xml") && !uploadFileName.endsWith(".xsd")) {
            return Response.status(400).entity("The file suffix must be .xml for cmdi records or .xsd for profiles.").type(MediaType.TEXT_PLAIN).build();
        }

        //read inputstream into string (from stackoverflow, like all code ever)
        String content = new BufferedReader(new InputStreamReader(fileInputStream))
                .lines().collect(Collectors.joining("\n"));

        try {
            String tempPath = System.getProperty("java.io.tmpdir") + "/" + System.currentTimeMillis() + "_" + uploadFileName;
            FileManager.writeToFile(tempPath, content);
            Report report;

            try {
                CurationModule cm = new CurationModule();
                report = uploadFileName.endsWith(".xml") ? cm.processCMDInstance(Paths.get(tempPath)) : cm.processCMDProfile(Paths.get(tempPath).toUri().toURL());
            } catch (MalformedURLException e) {
                return Response.status(400).entity("Input URL is malformed.").type(MediaType.TEXT_PLAIN).build();
            } catch (Exception e) {
                _logger.error("There was an exception processing the cmd instance: " + e.getMessage());
                return Response.status(400).entity("There was a problem when processing the input. Please make sure to upload a valid cmd file.").type(MediaType.TEXT_PLAIN).build();
            }

            if (report instanceof ErrorReport) {
                return Response.status(400).entity(((ErrorReport) report).error).type(MediaType.TEXT_PLAIN).build();
            }


            java.nio.file.Path xmlPath = Paths.get(Configuration.OUTPUT_DIRECTORY).resolve("xml").resolve("instances");
            Files.createDirectories(xmlPath);

            JAXBContext jc = JAXBContext.newInstance(report.getClass());

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");

            marshaller.marshal(report, Files.newOutputStream(xmlPath.resolve(report.getName() + ".xml")));


            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(Main.class.getResourceAsStream("/xslt/" + report.getClass().getSimpleName() + "2HTML.xsl"));

            Transformer transformer = factory.newTransformer(xslt);
            StreamResult result = new StreamResult(new StringWriter());
            transformer.transform(new JAXBSource(JAXBContext.newInstance(report.getClass()), report), result);

            return Response.ok(HtmlManipulator.addContentToGenericHTML(result.getWriter().toString(), null)).type(MediaType.TEXT_HTML).build();
        } catch (IOException e) {
            _logger.error("There was a problem generating the report: ", e);
            return Response.serverError().build();
        } catch (TransformerException ex) {

            return Response.serverError().build();
        } catch (JAXBException ex) {

            return Response.serverError().build();
        }
    }

}
