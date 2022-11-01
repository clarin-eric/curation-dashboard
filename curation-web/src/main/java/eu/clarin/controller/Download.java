package eu.clarin.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.clarin.helpers.ResponseManager;
import eu.clarin.main.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/download")
public class Download {

   @GetMapping("/{outputType}/{curationEntityType}/{reportName}")
   public Response getFile(@PathVariable("outputType") String outputType,
         @PathVariable("curationEntityType") String curationEntityType, @PathVariable("reportName") String reportName) {

      java.nio.file.Path xmlPath = Paths.get(Configuration.OUTPUT_DIRECTORY.toString(), "xml", curationEntityType,
            reportName + ".xml");

      try {

         if ("tsv".equalsIgnoreCase(outputType) || "json".equalsIgnoreCase(outputType)) {
            return ResponseManager.returnFile(200, (StreamingOutput) outputStream -> {
               String xslFileName = "tsv".equalsIgnoreCase(outputType)
                     ? "/xslt/" + reportName + "2" + outputType.toUpperCase() + ".xsl"
                     : "/xslt/XML2JSON.xsl";
               TransformerFactory factory = TransformerFactory.newInstance();

               Source xslt = new StreamSource(this.getClass().getResourceAsStream(xslFileName));

               try {
                  Transformer transformer = factory.newTransformer(xslt);
                  transformer.transform(new StreamSource(xmlPath.toFile()), new StreamResult(outputStream));
               }
               catch (TransformerException ex) {
                  throw new RuntimeException(ex);
               }
            }, "application/" + outputType, reportName + "." + outputType);
         }

         return ResponseManager.returnFile(200, Files.newInputStream(xmlPath), "application/xml",
               xmlPath.getFileName().toString());
      }
      catch (IOException ex) {
         log.error("couldn't write statistics to output", ex);
         return ResponseManager.returnServerError();
      }
   }

   @GetMapping("/{outputType}/statistics/{collectionName}/{category}")
   public Response getStatistics(@PathVariable("outputType") String outputType,
         @PathVariable("collectionName") String collectionName, @PathVariable("category") String category) {

      if (Arrays.asList("xml", "json", "tsv").contains(outputType) && !"overall".equalsIgnoreCase(category)) {
         try {
            Stream<CheckedLink> checkedLinkStream = Configuration.checkedLinkResource
                  .get(Configuration.checkedLinkResource.getCheckedLinkFilter()
                        .setProviderGroupIs(collectionName)
                        .setCategoryIs(Category.valueOf(category))
                        .setIsActive(true)
                     );
            return ResponseManager.returnFile(200, (StreamingOutput) outputStream -> {

               ZipOutputStream zipOutStream = new ZipOutputStream(outputStream);

               zipOutStream.putNextEntry(new ZipEntry(collectionName + "." + outputType));
               
               //start
               switch(outputType) {
               case "xml":
                  zipOutStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
                  zipOutStream.write(
                        String.format("<checkedLinks created-at=\"%1$tF %1$tT\" collection=\"%2$s\" category=\"%3$s\">\n", 
                              Calendar.getInstance(), 
                              collectionName, 
                              category).getBytes()
                        );
   
                  checkedLinkStream.forEach(checkedLink -> {
   
                     try {
                        zipOutStream.write(
                           String
                              .format(
                                 "   <link url=\"%1$s\" checkingDate=\"%2$tF %2$tT\" method=\"%3$s\" statusCode=\"%4$s\" byteSize=\"%5$s\" duration=\"%6$s\" redirects=\"%7$s\" message=\"%8$s\" collection=\"%9$s\" record=\"%10$s\" expected-mime-type=\"%11$s\" />\n",
                                 checkedLink.getUrl(), 
                                 checkedLink.getCheckingDate(), 
                                 checkedLink.getMethod(),
                                 checkedLink.getStatus(), 
                                 checkedLink.getByteSize(),
                                 checkedLink.getDuration(),
                                 checkedLink.getRedirectCount(),
                                 checkedLink.getMessage(),
                                 checkedLink.getProviderGroup(),
                                 checkedLink.getRecord(),
                                 checkedLink.getExpectedMimeType()
                              )
                              .getBytes()
                           );
                     }
                     catch (IOException e) {
   
                        log.error("can't write checkedLink: %1 to zip output", checkedLink.toString());
                     }
                  });
   
                  checkedLinkStream.close();
   
                  zipOutStream.write("</checkedLinks>".getBytes());
                  break;
                  
               case "json":
                  zipOutStream.write("{\n".getBytes());
                  zipOutStream.write(
                        String.format("   \"created-at\": \"%1$tF %1$tT\",\n   \"collection\": \"%2$s\",\n   \"category\": \"%3$s\",\n   \"link\": [", 
                              Calendar.getInstance(), 
                              collectionName, 
                              category).getBytes()
                        );
                  
                  AtomicBoolean isFirstLink = new AtomicBoolean(true);
                  
                  checkedLinkStream.forEach(checkedLink -> {
                     

   
                     try {
                        if(isFirstLink.get()) {
                           isFirstLink.set(false);
                        }
                        else {
                           zipOutStream.write(",".getBytes());
                        }                        
                        zipOutStream.write(
                              String.format(
                                 "\n      { \"url\": \"%1$s\", \"checkingDate\": \"%2$tF %2$tT\", \"method\": \"%3$s\", \"statusCode\": %4$s, \"byteSize\": %5$s, \"duration\": %6$s, \"redirects\": %7$s, \"message\": \"%8$s\", \"collection\": \"%9$s\", \"record\": \"%10$s\", \"expected-mime-type\": \"%11$s\" }",
                                 checkedLink.getUrl(), 
                                 checkedLink.getCheckingDate(), 
                                 checkedLink.getMethod(),
                                 checkedLink.getStatus(), 
                                 checkedLink.getByteSize(),
                                 checkedLink.getDuration(),
                                 checkedLink.getRedirectCount(),
                                 checkedLink.getMessage(),
                                 checkedLink.getProviderGroup(),
                                 checkedLink.getRecord(),
                                 checkedLink.getExpectedMimeType()                              
                              )
                              .getBytes()
                           );
                     }
                     catch (IOException e) {
   
                        log.error("can't write checkedLink: %1 to zip output", checkedLink.toString());
                     }
                  });
   
                  checkedLinkStream.close();
   
                  zipOutStream.write("\n   ]\n}".getBytes());                  
               break;
               
               case "tsv":
                  zipOutStream.write(
                        String.format("checkedLinks created-at: %1$tF %1$tT, collection: %2$s, category: %3$s\n", 
                              Calendar.getInstance(), 
                              collectionName, 
                              category).getBytes()
                        );
                  
                  zipOutStream.write("url\tcheckingDate\tmethod\tstatusCode\tbyteSize\tduration\tredirects\tmessage\tcollection\trecord\texpected-mime-type\n".getBytes());
   
                  checkedLinkStream.forEach(checkedLink -> {
   
                     try {
                        zipOutStream.write(
                           String
                              .format(
                                 "%1$s\t%2$tF %2$tT\t%3$s\t%4$s\t%5$s\t%6$s\t%7$s\t%8$s\t%9$s\t%10$s\t%11$s\n",
                                 checkedLink.getUrl(), 
                                 checkedLink.getCheckingDate(), 
                                 checkedLink.getMethod(),
                                 checkedLink.getStatus(), 
                                 checkedLink.getByteSize(),
                                 checkedLink.getDuration(),
                                 checkedLink.getRedirectCount(),
                                 checkedLink.getMessage(),
                                 checkedLink.getProviderGroup(),
                                 checkedLink.getRecord(),
                                 checkedLink.getExpectedMimeType()
                              )
                              .getBytes());
                     }
                     catch (IOException e) {
   
                        log.error("can't write checkedLink: %1 to zip output", checkedLink.toString());
                     }
                  });
   
                  checkedLinkStream.close();
                  break; 
               }
               
               zipOutStream.closeEntry();
               zipOutStream.close();

            }, "application/zip", collectionName + ".zip");

         }
         catch (Exception ex) {
            log.error("couldn't write statistics to output", ex);
         }
      }

      return ResponseManager.returnServerError();
   }

}
