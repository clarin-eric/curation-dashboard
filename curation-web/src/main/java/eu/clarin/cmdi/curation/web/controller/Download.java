package eu.clarin.cmdi.curation.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.web.dto.StatusDetailDto;
import eu.clarin.linkchecker.persistence.model.StatusDetail;
import eu.clarin.linkchecker.persistence.utils.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.BasicTransformerFactory;
import org.hibernate.jpa.AvailableHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The type Download.
 */
@Slf4j
@Controller
@RequestMapping("/download")
public class Download {

   /**
    * The Conf.
    */
   @Autowired
   ApiConfig conf;
   /**
    * The Em.
    */
   @PersistenceContext
   EntityManager em;

   /**
    * Gets file.
    *
    * @param curationEntityType the curation entity type
    * @param reportName         the report name
    * @param format             the format
    * @return the file
    */
   @RequestMapping(value = "/{curationEntityType}/{reportName}", method = {RequestMethod.GET, RequestMethod.POST})
   public ResponseEntity<StreamingResponseBody> getFile(@PathVariable("curationEntityType") String curationEntityType,
         @PathVariable("reportName") String reportName,
         @RequestHeader(name = "Accept", required = false) Optional<String> acceptHeader,
         @RequestParam(value = "format", required = false) Optional<String> format) {

      // requirement to use replace "collection" in path
      if("metadataprovider".equals(curationEntityType)){
         curationEntityType = "collection";
      }

      // if URL parameter format is not set, we take the accept header
      if(format.isEmpty() && acceptHeader.isPresent()){

         if (acceptHeader.get().startsWith("application/json")) {

            format = Optional.of("json");
         }
         else if (acceptHeader.get().startsWith("text/tab-separated-values") ){

            format = Optional.of("tsv");
         }
      }

      // if nothing is set neither in URL parameter format nor accept header or if it's no know return type we set xml by default
      if(format.isEmpty()){

         format = Optional.of("xml");
      }



      java.nio.file.Path xmlPath = conf.getDirectory().getOut().resolve("xml").resolve(curationEntityType)
            .resolve(reportName + ".xml");

      if (Files.notExists(xmlPath)) {

         return ResponseEntity.notFound().build();

      }
      
      final MediaType mediaType = switch(format.get()) {
      
         case "json" -> MediaType.APPLICATION_JSON;
         case "tsv" -> new MediaType("text", "tab-separated-values");
         default -> MediaType.APPLICATION_XML;
      };

      final String finalFormat = format.get();

      StreamingResponseBody stream = outputStream -> {

         if ("tsv".equalsIgnoreCase(finalFormat) || "json".equalsIgnoreCase(finalFormat)) {

            String xslFileName = "tsv".equalsIgnoreCase(finalFormat)
                  ? STR."/xslt/\{reportName}2\{finalFormat.toUpperCase()}.xsl"
                  : "/xslt/XML2JSON.xsl";
            TransformerFactory factory = BasicTransformerFactory.newInstance();

            Source xslt = new StreamSource(this.getClass().getResourceAsStream(xslFileName));

            try {
               Transformer transformer = factory.newTransformer(xslt);
               transformer.transform(new StreamSource(xmlPath.toFile()), new StreamResult(outputStream));
            }
            catch (TransformerException e) {

               throw new RuntimeException(
                     "can't create output in format '" + finalFormat + "' - please download as xml", e);

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

   /**
    * Gets file.
    *
    * @param providergroupName the providergroup name
    * @param category          the category
    * @param format            the format
    * @param zipped            the zipped
    * @return the file
    */
   @GetMapping("/linkchecker/{providergroupName}/{category}")
   public ResponseEntity<StreamingResponseBody> getFile(@PathVariable("providergroupName") String providergroupName,
         @PathVariable("category") Category category,
         @RequestHeader(name = "Accept", required = false) Optional<String> acceptHeader,
         @RequestParam(value = "format", required = false) Optional<String> format,
         @RequestParam(value = "zipped", required = false, defaultValue = "false") boolean zipped) {

      // if URL parameter format is not set, we take the accept header
      if(format.isEmpty() && acceptHeader.isPresent()){

         if (acceptHeader.get().startsWith("application/json")) {

            format = Optional.of("json");
         }
         else if (acceptHeader.get().startsWith("text/tab-separated-values") ){

            format = Optional.of("tsv");
         }
      }

      // if nothing is set neither in URL parameter format nor accept header or if it's no know return type we set xml by default
      if(format.isEmpty()){

         format = Optional.of("xml");
      }
      
      HttpHeaders headers = new HttpHeaders();
      
      
      headers.setContentType(new MediaType("application", zipped?"zip":format.get()));
      
      headers.setContentDisposition(
            ContentDisposition
               .inline()
               .filename(providergroupName + "-" + category + (zipped?".zip":"." + format.get()))
               .build()
            );

      final String finalFormat = format.get();
      return ResponseEntity.ok().headers(headers)
            .body(outputStream -> {
        
               if(zipped) {
                  ZipOutputStream zipOutStream = new ZipOutputStream(outputStream);
                  zipOutStream.putNextEntry(new ZipEntry(providergroupName + "." + finalFormat));

                  writeDetails(zipOutStream, providergroupName, category, finalFormat);
                  
                  zipOutStream.closeEntry();
                  zipOutStream.close();               
               }
               else {
                  
                  writeDetails(outputStream, providergroupName, category, finalFormat);
               }
            });      
   }
   
   private void writeDetails(OutputStream outputStream, String providergroupName, Category category, String format) throws IOException {
      
      String[] formatedString = {
            switch(format) {
               case "json" -> "{\n   \"creationDate\": \"%1$tF %1$tT\",\n   \"collection\": \"%2$s\",\n   \"category\": \"%3$s\",\n   \"link\": [";
               case "tsv" -> """
                       checkedLinks createdionDate: %1$tF %1$tT, collection: %2$s, category: %3$s
                       url\tcheckingDate\tmethod\tstatusCode\tbyteSize\tduration\tredirects\tmessage\tcollection\torigin\texpected-mime-type
                       """;
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
                  .setHint(AvailableHints.HINT_FETCH_SIZE, "1")
                  .setHint(AvailableHints.HINT_CACHEABLE, "false")
                  .setHint(AvailableHints.HINT_READ_ONLY, "true")
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
