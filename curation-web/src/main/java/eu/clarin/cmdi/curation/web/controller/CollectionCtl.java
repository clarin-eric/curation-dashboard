package eu.clarin.cmdi.curation.web.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/collection")
@Slf4j
public class Collection {

   @Autowired
   WebConfig conf;

   @GetMapping(value = {"", "/{collectionReportName}"})
   public String getCollection(@PathVariable(value = "collectionReportName", required = false) Optional<String> collectionReportName, Model model) {

      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("profile");
      
      if(collectionReportName.isPresent()) {
         
         reportPath = reportPath.resolve(Paths.get(collectionReportName.get()));
      
      }
      else {
         
         reportPath = reportPath.resolve("AllCollectionReport.html");
      
      }
      
      log.debug("reportPath: {}", reportPath);
      
      model.addAttribute("insert", reportPath.toString());

      return "generic";
   
   }
}
