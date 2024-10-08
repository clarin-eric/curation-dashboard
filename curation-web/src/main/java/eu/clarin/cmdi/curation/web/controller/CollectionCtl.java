package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * The type Collection ctl.
 */
@Controller
@RequestMapping("/collection")
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
   public String getCollection(@RequestHeader("Accept") String acceptHeader, @PathVariable(value = "collectionReportName", required = false) Optional<String> collectionReportName, Model model) {

      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("collection");
      
      if(collectionReportName.isPresent()) {

         if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("application/json")) {

            return "forward:/download/collection/" + collectionReportName.get() + "?format=json";
         }
         if(StringUtils.isNotEmpty(acceptHeader) && (acceptHeader.startsWith("application/xml") || acceptHeader.startsWith("text/xml"))) {

            return "forward:/download/collection/" + collectionReportName.get() ;
         }

         reportPath = reportPath.resolve(Paths.get(collectionReportName.get()));
      
      }
      else {

         if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("application/json")) {

            return "forward:/download/collection/AllCollectionReport?format=json";
         }
         if(StringUtils.isNotEmpty(acceptHeader) && (acceptHeader.startsWith("application/xml") || acceptHeader.startsWith("text/xml"))) {

            return "forward:/download/collection/AllCollectionReport";
         }
         if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("text/tab-separated-values")) {

            return "forward:/download/collection/AllCollectionReport?format=tsv";
         }
         
         reportPath = reportPath.resolve("AllCollectionReport.html");
      
      }
      
      log.debug("reportPath: {}", reportPath);
      
      model.addAttribute("insert", reportPath.toString());

      return "generic";
   
   }
}
