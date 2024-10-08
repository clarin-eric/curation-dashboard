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
   public String getProfile(@RequestHeader("Accept") String acceptHeader, @PathVariable(value = "profileReportName") Optional<String> profileReportName, Model model) {

      String returnString = null;
      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("profile");
      
      if(profileReportName.isPresent()) {

         if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("application/json")) {

            return "forward:/download/profile/" + profileReportName.get() + "?format=json";
         }
         if(StringUtils.isNotEmpty(acceptHeader) && (acceptHeader.startsWith("application/xml") || acceptHeader.startsWith("text/xml"))) {

            return "forward:/download/profile/" + profileReportName.get() ;
         }
         
         reportPath = reportPath.resolve(Paths.get(profileReportName.get()));
      
      }
      else {

         if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("application/json")) {

            return "forward:/download/profile/AllProfileReport?format=json";
         }
         if(StringUtils.isNotEmpty(acceptHeader) && (acceptHeader.startsWith("application/xml") || acceptHeader.startsWith("text/xml"))) {

            return "forward:/download/profile/AllProfileReport";
         }
         if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("text/tab-separated-values")) {

            return "forward:/download/profile/AllProfileReport?format=tsv";
         }
         
         reportPath = reportPath.resolve("AllProfileReport.html");
      
      }
      
      log.debug("reportPath: {}", reportPath);
      
      model.addAttribute("insert", reportPath.toString());

      return "generic";
   }
}
