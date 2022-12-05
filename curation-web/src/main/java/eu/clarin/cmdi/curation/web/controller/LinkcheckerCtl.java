/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.web.controller;

import java.nio.file.Path;
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
 *
 */
@Controller
@RequestMapping("linkchecker")
@Slf4j
public class LinkcheckerCtl {
   
   @Autowired
   WebConfig conf;
   

   @GetMapping()
   public String getReport(Model model) {
      
      Path reportPath = conf.getDirectory().getOut()
            .resolve("html")
            .resolve("linkchecker")
            .resolve("AllLinkcheckerReport.html");
      
      log.debug("reportPath: {}", reportPath);
      
      model.addAttribute("insert", reportPath.toString());
      
      return "generic";
      
   }
   
   @GetMapping("/{providergroupName}")
   public String getReport(@PathVariable(value = "providergroupName") String providergroupName, Model model) {

      Path reportPath = conf.getDirectory().getOut()
            .resolve("html")
            .resolve("linkchecker")
            .resolve(providergroupName + ".html");
      
      model.addAttribute("insert", reportPath.toString());   
      
      return "generic";
   }


}
