package eu.clarin.routes;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.main.Main;
import eu.clarin.cmdi.curation.report.ErrorReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.utils.FileNameEncoder;
import eu.clarin.helpers.FileManager;
import eu.clarin.helpers.HTMLHelpers.HtmlManipulator;
import eu.clarin.helpers.ResponseManager;
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
    public Response getInstanceQueryParam(@QueryParam("url-input") String urlStr) {

        if (urlStr == null || urlStr.isEmpty()) {
            return ResponseManager.returnError(400, "Input URL can't be empty.");
        }

        try {

            Report report;

            URL url = new URL(urlStr);

            byte[] buffer = new byte[200];

            int offset = 0;
            InputStream in = url.openStream();


            while ((offset += in.read(buffer, offset, 200 - offset)) < 200)
                ;

            in.close();

            String content = new String(buffer);


            try {
                CurationModule cm = new CurationModule();
                report = !content.substring(0, 200).contains("xmlns:xs=") ? cm.processCMDInstance(url) : cm.processCMDProfile(url);
            } catch (MalformedURLException e) {
                return ResponseManager.returnError(400, "Input URL is malformed.");

            } catch (Exception e) {
                _logger.error("There was an exception processing the cmd instance: " + e.getMessage());
                return ResponseManager.returnError(400, "There was a problem when processing the input. Please make sure to upload a valid cmd file.");
            }

            if (report instanceof ErrorReport) {
                return ResponseManager.returnError(400, ((ErrorReport) report).error);
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


            StringWriter writeBuffer = new StringWriter();


            StreamResult result = new StreamResult(writeBuffer);

            transformer.transform(new JAXBSource(JAXBContext.newInstance(report.getClass()), report), result);

            String htmlFilePath = Configuration.OUTPUT_DIRECTORY + "/html/instances/" + report.getName() + ".html";
            FileManager.writeToFile(htmlFilePath, writeBuffer.toString());

            return ResponseManager.returnHTML(200, result.getWriter().toString(), null);
        } catch (IOException | TransformerException | JAXBException e) {
            _logger.error("There was a problem generating the report: ", e);
            return ResponseManager.returnServerError();
        }
    }


    //this is for drag and drop instance form
    @POST
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response postInstance(@FormDataParam("file") InputStream fileInputStream,
                                 @FormDataParam("file") FormDataContentDisposition fileMetaData) {


        String uploadFileName = fileMetaData.getFileName();
        /*
         * if (!uploadFileName.endsWith(".xml") && !uploadFileName.endsWith(".xsd")) {
         * return Response.status(400).
         * entity("The file suffix must be .xml for cmdi records or .xsd for profiles.")
         * .type(MediaType.TEXT_PLAIN).build(); }
         */

        //read inputstream into string (from stackoverflow, like all code ever)
        String content = new BufferedReader(new InputStreamReader(fileInputStream))
                .lines().collect(Collectors.joining("\n"));

        try {
            String tempPath = System.getProperty("java.io.tmpdir") + "/" + System.currentTimeMillis() + "_" + uploadFileName;
            FileManager.writeToFile(tempPath, content);
            Report report;

            try {
                CurationModule cm = new CurationModule();
                report = !content.substring(0, 200).contains("xmlns:xs=") ? cm.processCMDInstance(Paths.get(tempPath)) : cm.processCMDProfile(Paths.get(tempPath).toUri().toURL());
            } catch (MalformedURLException e) {
                return ResponseManager.returnError(400, "Input URL is malformed.");
            } catch (Exception e) {
                _logger.error("There was an exception processing the cmd instance: " + e.getMessage());
                return ResponseManager.returnError(400, "There was a problem when processing the input. Please make sure to upload a valid cmd file.");
            }

            if (report instanceof ErrorReport) {
                return ResponseManager.returnError(400, ((ErrorReport) report).error);
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


            StringWriter writeBuffer = new StringWriter();


            StreamResult result = new StreamResult(writeBuffer);

            transformer.transform(new JAXBSource(JAXBContext.newInstance(report.getClass()), report), result);

            String htmlFilePath = Configuration.OUTPUT_DIRECTORY + "/html/instances/" + report.getName() + ".html";
            FileManager.writeToFile(htmlFilePath, writeBuffer.toString());

            return ResponseManager.returnHTML(200, result.getWriter().toString(), null);
        } catch (IOException | TransformerException | JAXBException e) {
            _logger.error("There was a problem generating the report: ", e);
            return ResponseManager.returnServerError();
        }
    }
}
