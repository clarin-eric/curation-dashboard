package eu.clarin.cmdi.curation.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.linkchecker.persistence.repository.StatusRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/download")
public class Download {

   @Autowired
   ApiConfig conf;
   @Autowired
   StatusRepository sRep;

   @GetMapping("/{curationEntityType: profile|collection|linkchecker}/{reportName}")
   public ResponseEntity<StreamingResponseBody> getFile(@PathVariable("curationEntityType") String curationEntityType,
         @PathVariable("reportName") String reportName,
         @RequestParam(value = "contentType", required = false) String contentType) {

      java.nio.file.Path xmlPath = conf.getDirectory().getOut().resolve("xml").resolve(curationEntityType)
            .resolve(reportName + ".xml");

      if (Files.notExists(xmlPath)) {

         throw new RuntimeException("no report for entity '" + reportName + "'");

      }

      StreamingResponseBody stream = outputStream -> {

         if ("tsv".equalsIgnoreCase(contentType) || "json".equalsIgnoreCase(contentType)) {

            String xslFileName = "tsv".equalsIgnoreCase(contentType)
                  ? "/xslt/" + reportName + "2" + contentType.toUpperCase() + ".xsl"
                  : "/xslt/XML2JSON.xsl";
            TransformerFactory factory = TransformerFactory.newInstance();

            Source xslt = new StreamSource(this.getClass().getResourceAsStream(xslFileName));

            try {
               Transformer transformer = factory.newTransformer(xslt);
               transformer.transform(new StreamSource(xmlPath.toFile()), new StreamResult(outputStream));
            }
            catch (TransformerException e) {

               throw new RuntimeException(
                     "can't create output in format '" + contentType + "' - please download as xml", e);

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
      
      return ResponseEntity.ok(stream);
   
   }
}
