package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import eu.clarin.cmdi.curation.web.exception.NoSuchReportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The type Collection ctl.
 */
@Controller
@RequestMapping(value = {"/", "/collection", "/metadataprovider"})
@Slf4j
public class CollectionCtl {

   /**
    * The Conf.
    */
   @Autowired
   WebConfig conf;

   /**
    * Gets collection.
    *
    * @param collectionReportName the collection report name
    * @param model                the model
    * @return the collection
    */
   @GetMapping(value = {"", "/{collectionReportName}"})
   public String getCollection(@RequestHeader(name = "Accept", required = false) Optional<String> acceptHeader, @RequestParam(name = "format", required = false) Optional <String> format, @PathVariable(value = "collectionReportName", required = false) Optional<String> collectionReportName, Model model) {

      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("collection");
      
      if(collectionReportName.isPresent()) {

         String basename = FilenameUtils.getBaseName(collectionReportName.get());

         if(format.isPresent() ||
                 ( acceptHeader.isPresent() &&
                         (acceptHeader.get().startsWith("application/json") ||
                                 acceptHeader.get().startsWith("application/xml") ||
                                 acceptHeader.get().startsWith("text/xml")
                         )
                 )
         ) {

            return "forward:/download/collection/" + basename;
         }

         reportPath = reportPath.resolve(basename + ".html");
      
      }
      else { // without specific collectionReportName we return the AllCollectionReport.html

         if(format.isPresent() ||
                 ( acceptHeader.isPresent() &&
                         (acceptHeader.get().startsWith("application/json") ||
                                 acceptHeader.get().startsWith("application/xml") ||
                                 acceptHeader.get().startsWith("text/xml") ||
                                 acceptHeader.get().startsWith("text/tab-separated-values")
                         )
                 )
         ) {

            return "forward:/download/collection/AllCollectionReport";
         }
         
         reportPath = reportPath.resolve("AllCollectionReport.html");
      
      }

      if(Files.notExists(reportPath)){

         throw new NoSuchReportException();
      }
      
      model.addAttribute("insert", reportPath.toString());

      return "generic";
   
   }

    /**
     * Gets robots txt.
     *
     * @return the robots txt
     */
    @GetMapping("/robots.txt")
    public ResponseEntity<String> getRobotsTxt() {
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(
                        """
                           User-agent: *
                           Disallow: /download
                           Disallow: /record
                           
                           User-agent: CLARIN-Linkchecker
                           Allow: /
                           """
                );
    }
}
