package eu.clarin.routes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;
import eu.clarin.helpers.CheckedLinkStAXReader;
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
                     catch (TransformerException ex) {
                       throw new RuntimeException(ex);
                     }
                  },
                  "application/" + outputType,
                  reportName + "." + outputType);
         }
         
         return ResponseManager.returnFile(200,Files.newInputStream(xmlPath),"application/xml",xmlPath.getFileName().toString());
      }
      catch (IOException ex) {
         log.error("couldn't write statistics to output", ex);
         return ResponseManager.returnServerError();
      }     
   }
   
   @GET
   @Path("/{outputType}/statistics/{collectionName}/{category}")
   public Response getStatistics(@PathParam("outputType") String outputType, @PathParam("collectionName") String collectionName, @PathParam("category") String category) {
      
      if(!"overall".equalsIgnoreCase(category)) {   
         try {
            Stream<CheckedLink> checkedLinkStream = Configuration.checkedLinkResource.get(
                  Configuration.checkedLinkResource.getCheckedLinkFilter()
                     .setProviderGroupIs(collectionName)
                     .setCategoryIs(Category.valueOf(category))
                     .setIsActive(true)   
                     );
            return ResponseManager.returnFile(
                  200, 
                  (StreamingOutput) outputStream -> {
                     TransformerFactory factory = TransformerFactory.newInstance();
   
                     try {
                        
                        Transformer transformer = null;
                     
                        if("json".equalsIgnoreCase(outputType)) {
                           Source xslt = new StreamSource(this.getClass().getResourceAsStream("/xslt/XML2JSON.xsl"));
                           transformer = factory.newTransformer(xslt);
                        }
                        else {
                           transformer = factory.newTransformer();
                        }
                        transformer.transform(new StAXSource(new CheckedLinkStAXReader(checkedLinkStream)), new StreamResult(outputStream));
                     }
                     catch (TransformerException e) {
                        throw new RuntimeException(e);
                     }

                  },
                  "application/" + outputType,
                  collectionName + "." + outputType);
         }
         catch(Exception ex) {
            log.error("couldn't write statistics to output", ex);
         }
      }
      
      return ResponseManager.returnServerError();
   }
}
