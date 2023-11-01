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
   public String getProfile(@PathVariable(value = "profileReportName") Optional<String> profileReportName, Model model) {
      
      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("profile");
      
      if(profileReportName.isPresent()) {
         
         reportPath = reportPath.resolve(Paths.get(profileReportName.get()));
      
      }
      else {
         
         reportPath = reportPath.resolve("AllProfileReport.html");
      
      }
      
      log.debug("reportPath: {}", reportPath);
      
      model.addAttribute("insert", reportPath.toString());

      return "generic";
   }
}
