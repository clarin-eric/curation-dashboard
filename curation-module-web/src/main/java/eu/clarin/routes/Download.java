package eu.clarin.routes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/download")
public class Download {
   
   @GET
   @Path("/{outputType}/{curationEntityType}/{reportName}")
   public Response getFile(@PathParam("outputType") String outputType, @PathParam("curationEntityType") String curationEntityType, @PathParam("reportName") String reportName) {
      
      java.nio.file.Path xmlPath = Paths.get(Configuration.OUTPUT_DIRECTORY.toString(), "xml", curationEntityType, reportName + ".xml");
       
      try {
         
         if("tsv".equalsIgnoreCase(outputType) || "json".equalsIgnoreCase(outputType)) {
            return ResponseManager.returnFile(
                  200, 
                  (StreamingOutput) outputStream -> {
                     String xslFileName = "tsv".equalsIgnoreCase(outputType)?"/xslt/" + reportName + "2" + outputType.toUpperCase() + ".xsl":"/xslt/XML2JSON.xsl";
                     TransformerFactory factory = TransformerFactory.newInstance();
                     
                     Source xslt = new StreamSource(this.getClass().getResourceAsStream(xslFileName));

                     try {
                        Transformer transformer = factory.newTransformer(xslt);
                        transformer.transform(new StreamSource(xmlPath.toFile()), new StreamResult(outputStream));
                     }
                     catch (TransformerConfigurationException e) {
                        log.error("", e);
                     }
                     catch (TransformerException e) {
                        log.error("", e);
                     }
                  },
                  "application/" + outputType,
                  reportName + "." + outputType);
         }
         
         return ResponseManager.returnFile(200,Files.newInputStream(xmlPath),"application/xml",xmlPath.getFileName().toString());
      }
      catch (IOException e) {
         log.error("", e);
         return ResponseManager.returnServerError();
      }     
   }
}
