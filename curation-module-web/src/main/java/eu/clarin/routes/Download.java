package eu.clarin.routes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
   @Path("/zip/statistics/{collectionName}/{category}")
   public Response getStatistics(@PathParam("collectionName") String collectionName, @PathParam("category") String category) {
      
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
                     
                     ZipOutputStream zipOutStream = new ZipOutputStream(outputStream);
                     
                     zipOutStream.putNextEntry(new ZipEntry(collectionName + ".xml"));
                     
                     zipOutStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
                     zipOutStream.write(String.format("<checkedLinks created-at=\"%1$tF %1$tT\">\n", Calendar.getInstance()).getBytes());
                     
                     checkedLinkStream.forEach(checkedLink -> {
                        
                        try {
                           zipOutStream.write(String.format(
                                 "   <link url=\"%1$s\" checkingDate=\"%2$tF %2$tT\" method=\"%3$s\" statusCode=\"%4$s\" category=\"%5$s\" byteSize=\"%6$s\" duration=\"%7$s\" message=\"%8$s\" />\n", 
                                 checkedLink.getUrl(), 
                                 checkedLink.getCheckingDate(), 
                                 checkedLink.getMethod(),
                                 (checkedLink.getStatus()!=null?checkedLink.getStatus():""),
                                 checkedLink.getCategory(), 
                                 (checkedLink.getByteSize()!=null?checkedLink.getByteSize():""),
                                 (checkedLink.getDuration()!=null?checkedLink.getDuration():""),
                                 (checkedLink.getMessage()!=null?checkedLink.getMessage():"")
                              ).getBytes());
                        }
                        catch (IOException e) {
                           
                           log.error("can't write checkedLink: %1 to zip output", checkedLink.toString());
                        }                        
                     });
                     
                     checkedLinkStream.close();
                     
                     zipOutStream.write("</checkedLinks>".getBytes());
                     zipOutStream.closeEntry();
                     zipOutStream.close();
   

                  },
                  "application/zip",
                  collectionName + ".zip");           

         }
         catch(Exception ex) {
            log.error("couldn't write statistics to output", ex);
         }
      }
      
      return ResponseManager.returnServerError();
   }

}
