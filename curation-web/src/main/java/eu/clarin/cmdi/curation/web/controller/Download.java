package eu.clarin.cmdi.curation.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.hibernate.jpa.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.web.dto.StatusDetailDto;
import eu.clarin.linkchecker.persistence.model.StatusDetail;
import eu.clarin.linkchecker.persistence.utils.Category;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/download")
public class Download {

   @Autowired
   ApiConfig conf;
   @PersistenceContext
   EntityManager em;

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
         @RequestParam(value = "format", required = false, defaultValue = "xml") String format,
         @RequestParam(value = "zipped", required = false, defaultValue = "false") boolean zipped) {
      
      HttpHeaders headers = new HttpHeaders();
      
      
      headers.setContentType(new MediaType("application", zipped?"zip":format));
      
      headers.setContentDisposition(
            ContentDisposition
               .inline()
               .filename(providergroupName + "-" + category + (zipped?".zip":"." + format))
               .build()
            );
      
      return ResponseEntity.ok().headers(headers)
            .body(outputStream -> {
        
               if(zipped) {
                  ZipOutputStream zipOutStream = new ZipOutputStream(outputStream);
                  zipOutStream.putNextEntry(new ZipEntry(providergroupName + "." + format));

                  writeDetails(zipOutStream, providergroupName, category, format);                  
                  
                  zipOutStream.closeEntry();
                  zipOutStream.close();               
               }
               else {
                  
                  writeDetails(outputStream, providergroupName, category, format);
               }
            });      
   }
   
   private void writeDetails(OutputStream outputStream, String providergroupName, Category category, String format) throws IOException {
      
      String[] formatedString = {
            switch(format) {
               case "json" -> "{\n   \"creationDate\": \"%1$tF %1$tT\",\n   \"collection\": \"%2$s\",\n   \"category\": \"%3$s\",\n   \"link\": [";
               case "tsv" -> "checkedLinks createdionDate: %1$tF %1$tT, collection: %2$s, category: %3$s\n"
                     + "url\tcheckingDate\tmethod\tstatusCode\tbyteSize\tduration\tredirects\tmessage\tcollection\torigin\texpected-mime-type\n";
               default ->  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<checkedLinks creationDate=\"%1$tF %1$tT\" collection=\"%2$s\" category=\"%3$s\">\n";
            }
         };

      // writing xml root open element, embracing json object or tsv table header 
      outputStream.write(String.format(formatedString[0], Calendar.getInstance(), providergroupName, category).getBytes());

      AtomicInteger lineNr = new AtomicInteger();   
      
      // preparing object serializer for xml, json or tsv
      final ObjectWriter writer = switch(format) {
      
         case "json" -> new ObjectMapper().writer();
         case "tsv" -> {            
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = mapper.schemaFor(StatusDetailDto.class).withColumnSeparator('\t');
            yield mapper.writer(schema);
         }
         default -> new XmlMapper().writer();
      };
      
      // we have to use the EntityManger directly to make streaming work
      Query query = !"overall".equalsIgnoreCase(providergroupName)?
         em.createNativeQuery(              
               """
               SELECT NULL AS order_nr, s.*, u.name AS urlname, p.name AS providergroupname, c.origin, uc.expected_mime_type
                  FROM status s 
                  INNER JOIN url u ON s.url_id = u.id 
                  INNER JOIN url_context uc ON uc.url_id = u.id
                  INNER JOIN context c ON c.id = uc.context_id
                  INNER JOIN providergroup p ON p.id = c.providergroup_id
                  WHERE s.category = ?1
                  AND p.name = ?2
                  AND uc.active = true       
                  """, 
                  StatusDetail.class)
               .setParameter(2, providergroupName):
         em.createNativeQuery(
               """
               SELECT NULL AS order_nr, s.*, u.name AS urlname, p.name AS providergroupname, c.origin, uc.expected_mime_type
                  FROM status s 
                  INNER JOIN url u ON s.url_id = u.id 
                  INNER JOIN url_context uc ON uc.url_id = u.id
                  INNER JOIN context c ON c.id = uc.context_id
                  INNER JOIN providergroup p ON p.id = c.providergroup_id
                  WHERE s.category = ?1
                  AND uc.active = true       
                  """, StatusDetail.class);
      
      try(
            // the hints are necessary for streaming
            @SuppressWarnings("unchecked")
            Stream<StatusDetail> sdStream = query
                  .setParameter(1, category.name())
                  .setHint(QueryHints.HINT_FETCH_SIZE, "1")
                  .setHint(QueryHints.HINT_CACHEABLE, "false")
                  .setHint(QueryHints.HINT_READONLY, "true")
                  .getResultStream();  
            )
      {
         
         sdStream.map(StatusDetailDto::new).forEach(detail -> {
            try {
               
               if("json".equals(format) && lineNr.incrementAndGet() > 1) {
                  outputStream.write(',');
               }
               
               outputStream.write(writer.writeValueAsBytes(detail));
               outputStream.flush();
            }
            catch (IOException e) {
               // outputStream is closed and we want to get out of the lambda loop without need to log
               throw new RuntimeException(e);
            }
         });// end stream 
      }// end try-catch
      
      formatedString[0] = switch(format) {
         case "json" -> "\n   ]\n}";
         case "tsv" -> "";
         default -> "</checkedLinks>";
      };
      
      outputStream.write(formatedString[0].getBytes());
   }
}
