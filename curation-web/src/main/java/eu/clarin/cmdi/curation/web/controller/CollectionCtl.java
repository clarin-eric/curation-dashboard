package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import eu.clarin.cmdi.curation.web.exception.NoSuchReportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * The type Collection ctl.
 */
@Controller
@RequestMapping(value = {"/collection", "/metadataprovider"})
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
   public String getCollection(@RequestHeader("Accept") Optional<String> acceptHeader, @PathVariable(value = "collectionReportName", required = false) Optional<String> collectionReportName, Model model) {

      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("collection");
      
      if(collectionReportName.isPresent()) {

         String basename = FilenameUtils.getBaseName(collectionReportName.get());
         String extension = FilenameUtils.getExtension(collectionReportName.get());

         if(acceptHeader.isPresent() && acceptHeader.get().startsWith("application/json") || "json".equals(extension)) {

            return "forward:/download/collection/" + basename + "?format=json";
         }
         if(acceptHeader.isPresent() && (acceptHeader.get().startsWith("application/xml") || acceptHeader.get().startsWith("text/xml")) || "xml".equals(extension)) {

            return "forward:/download/collection/" + basename ;
         }

         reportPath = reportPath.resolve(basename + ".html");
      
      }
      else { // without specific collectionReportName we return the AllCollectionReport.html

         if(acceptHeader.isPresent() && acceptHeader.get().startsWith("application/json")) {

            return "forward:/download/collection/AllCollectionReport?format=json";
         }
         if(acceptHeader.isPresent() && (acceptHeader.get().startsWith("application/xml") || acceptHeader.get().startsWith("text/xml"))) {

            return "forward:/download/collection/AllCollectionReport";
         }
         if(acceptHeader.isPresent() && acceptHeader.get().startsWith("text/tab-separated-values")) {

            return "forward:/download/collection/AllCollectionReport?format=tsv";
         }
         
         reportPath = reportPath.resolve("AllCollectionReport.html");
      
      }

      if(Files.notExists(reportPath)){

         throw new NoSuchReportException();
      }
      
      model.addAttribute("insert", reportPath.toString());

      return "generic";
   
   }
}
