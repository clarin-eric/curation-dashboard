package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import eu.clarin.cmdi.curation.web.exception.NoSuchReportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * The type Profile ctl.
 */
@Controller
@RequestMapping("/profile")
@Slf4j
public class ProfileCtl {

   /**
    * The Conf.
    */
   @Autowired
   WebConfig conf;

   /**
    * Gets profile.
    *
    * @param profileReportName the profile report name
    * @param model             the model
    * @return the profile
    */
   @GetMapping(value = {"", "/{profileReportName}"})
   public String getProfile(@RequestHeader(name = "Accept", required = false) Optional<String> acceptHeader, @RequestParam(name = "format", required = false) Optional <String> format, @PathVariable(value = "profileReportName") Optional<String> profileReportName, Model model) {

      String returnString = null;
      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("profile");
      
      if(profileReportName.isPresent()) {

         String basename = FilenameUtils.getBaseName(profileReportName.get());

         if(format.isPresent() ||
                 ( acceptHeader.isPresent() &&
                         (acceptHeader.get().startsWith("application/json") ||
                                 acceptHeader.get().startsWith("application/xml") ||
                                 acceptHeader.get().startsWith("text/xml")
                         )
                 )
         ) {

            return "forward:/download/profile/" + basename;
         }
         
         reportPath = reportPath.resolve(basename + ".html");
      
      }
      else {

         if(format.isPresent() ||
                 ( acceptHeader.isPresent() &&
                         (acceptHeader.get().startsWith("application/json") ||
                                 acceptHeader.get().startsWith("application/xml") ||
                                 acceptHeader.get().startsWith("text/xml") ||
                                 acceptHeader.get().startsWith("text/tab-separated-values")
                         )
                 )
         ) {

            return "forward:/download/profile/AllProfileReport";
         }
         
         reportPath = reportPath.resolve("AllProfileReport.html");
      
      }

      if(Files.notExists(reportPath)){

         throw new NoSuchReportException();
      }
      
      model.addAttribute("insert", reportPath.toString());

      return "generic";
   }
}
