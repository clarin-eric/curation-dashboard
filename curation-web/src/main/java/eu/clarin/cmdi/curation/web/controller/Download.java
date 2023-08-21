package eu.clarin.cmdi.curation.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.linkchecker.persistence.model.StatusDetail;
import eu.clarin.linkchecker.persistence.service.StatusService;
import eu.clarin.linkchecker.persistence.utils.Category;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/download")
public class Download {

   @Autowired
   ApiConfig conf;
   @Autowired
   StatusService sService;

   @GetMapping("/{curationEntityType}/{reportName}")
   public ResponseEntity<StreamingResponseBody> getFile(@PathVariable("curationEntityType") String curationEntityType,
         @PathVariable("reportName") String reportName,
         @RequestParam(value = "format", required = false, defaultValue = "xml") String format) {

      java.nio.file.Path xmlPath = conf.getDirectory().getOut().resolve("xml").resolve(curationEntityType)
            .resolve(reportName + ".xml");

      if (Files.notExists(xmlPath)) {

         throw new RuntimeException("no report for entity '" + reportName + "'");

      }
      
      final MediaType mediaType = switch(format) {      
      
         case "json" -> MediaType.APPLICATION_JSON;
         case "tsv" -> new MediaType("text", "tab-separated-values");
         default -> MediaType.APPLICATION_XML;
      };

      StreamingResponseBody stream = outputStream -> {

         if ("tsv".equalsIgnoreCase(format) || "json".equalsIgnoreCase(format)) {

            String xslFileName = "tsv".equalsIgnoreCase(format)
                  ? "/xslt/" + reportName + "2" + format.toUpperCase() + ".xsl"
                  : "/xslt/XML2JSON.xsl";
            TransformerFactory factory = TransformerFactory.newInstance();

            Source xslt = new StreamSource(this.getClass().getResourceAsStream(xslFileName));

            try {
               Transformer transformer = factory.newTransformer(xslt);
               transformer.transform(new StreamSource(xmlPath.toFile()), new StreamResult(outputStream));
            }
            catch (TransformerException e) {

               throw new RuntimeException(
                     "can't create output in format '" + format + "' - please download as xml", e);

            }
         }
         else {

            try (InputStream in = Files.newInputStream(xmlPath)) {

               int b = -1;

               while ((b = in.read()) != -1) {
                  outputStream.write(b);
               }

            }
            catch (IOException e) {
               
               log.error("can't read report '{}'", xmlPath);
               throw new RuntimeException("internal error - please contact Clarin");
            
            }
         }
      };
      
      return ResponseEntity.ok().contentType(new MediaType(mediaType)).body(stream);
   
   }
   
   @GetMapping("/linkchecker/{providergroupName}/{category}")
   public ResponseEntity<StreamingResponseBody> getFile(@PathVariable("providergroupName") String providergroupName,
         @PathVariable("category") Category category,
         @RequestParam(value = "format", required = false, defaultValue = "xml") String format) {
      
      HttpHeaders headers = new HttpHeaders();
      
      
      headers.setContentType(new MediaType("application", "zip"));
      
      headers.setContentDisposition(
            ContentDisposition
               .inline()
               .filename(providergroupName + "-" + category + ".zip")
               .build()
            );
      
      
      
      return ResponseEntity.ok().headers(headers)
            .body(outputStream -> {
         
               ZipOutputStream zipOutStream = new ZipOutputStream(outputStream);
      
               zipOutStream.putNextEntry(new ZipEntry(providergroupName + "." + format));
               
               String[] formatedString = {
                     switch(format) {
                        case "json" -> "{\n   \"creationDate\": \"%1$tF %1$tT\",\n   \"collection\": \"%2$s\",\n   \"category\": \"%3$s\",\n   \"link\": [";
                        case "tsv" -> "checkedLinks createdionDate: %1$tF %1$tT, collection: %2$s, category: %3$s\n"
                              + "url\tcheckingDate\tmethod\tstatusCode\tbyteSize\tduration\tredirects\tmessage\tcollection\torigin\texpected-mime-type\n";
                        default ->  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<checkedLinks creationDate=\"%1$tF %1$tT\" collection=\"%2$s\" category=\"%3$s\">\n";
                     }
                  };
               
               zipOutStream.write(String.format(formatedString[0], Calendar.getInstance(), providergroupName, category).getBytes());
               
               formatedString[0] = switch(format) {
                  case "json" -> "\n      { \"url\": \"%1$s\", \"checkingDate\": \"%2$tF %2$tT\", \"method\": \"%3$s\", \"statusCode\": %4$s, \"byteSize\": %5$s, \"duration\": %6$s, \"redirects\": %7$s, \"message\": \"%8$s\", \"collection\": \"%9$s\", \"origin\": \"%10$s\", \"expected-mime-type\": \"%11$s\" }";
                  case "tsv" -> "%1$s\t%2$tF %2$tT\t%3$s\t%4$s\t%5$s\t%6$s\t%7$s\t%8$s\t%9$s\t%10$s\t%11$s\n";
                  default -> "   <link url=\"%1$s\" checkingDate=\"%2$tF %2$tT\" method=\"%3$s\" statusCode=\"%4$s\" byteSize=\"%5$s\" duration=\"%6$s\" redirects=\"%7$s\" message=\"%8$s\" collection=\"%9$s\" origin=\"%10$s\" expected-mime-type=\"%11$s\" />\n";
               };
               
               
               try(
                     Stream<StatusDetail> sdStream = ("overall".equalsIgnoreCase(providergroupName)? sService.findAllDetail(category):sService.findAllDetail(providergroupName, category));
                     )
               {
                  
                  sdStream.forEach(detail -> {
                     try {
                        zipOutStream.write(
                              String.format(
                                 formatedString[0],
                                 detail.getUrlname(), 
                                 detail.getCheckingDate(), 
                                 detail.getMethod(),
                                 detail.getStatusCode(), 
                                 detail.getContentLength(),
                                 detail.getDuration(),
                                 detail.getRedirectCount(),
                                 detail.getMessage(),
                                 detail.getProvidergroupname(),
                                 detail.getOrigin(),
                                 detail.getExpectedMimeType()                              
                              )
                              .getBytes()
                           );
                     }
                     catch (IOException e) {
                        
                        log.error("can't write checkedLink: %1 to zip output", detail.toString());
                     }
                  });// end stream 
               }// end try-catch
               
               formatedString[0] = switch(format) {
                  case "json" -> "\n   ]\n}";
                  case "tsv" -> "";
                  default -> "</checkedLinks>";
               };
               
               zipOutStream.write(formatedString[0].getBytes());
               
               zipOutStream.closeEntry();
               zipOutStream.close();
            });
      
   }
}
