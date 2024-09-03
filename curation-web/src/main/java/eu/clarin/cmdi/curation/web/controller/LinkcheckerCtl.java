/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Map;

/**
 * The type Linkchecker ctl.
 */
@Controller
@RequestMapping("/linkchecker")
@Slf4j
public class LinkcheckerCtl {

   /**
    * The Conf.
    */
   @Autowired
   WebConfig conf;

   /**
    * Gets report.
    *
    * @param model the model
    * @return the report
    */
   @GetMapping()
   public String getReport(@RequestHeader("Accept") String acceptHeader, Model model) {

      if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("application/json")) {

         return "forward:/download/linkchecker/AllLinkcheckerReport?format=json";
      }
      if(StringUtils.isNotEmpty(acceptHeader) && (acceptHeader.startsWith("application/xml") || acceptHeader.startsWith("text/xml"))) {

         return "forward:/download/linkchecker/AllLinkcheckerReport";
      }

      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("linkchecker")
            .resolve("AllLinkcheckerReport.html");

      log.debug("reportPath: {}", reportPath);

      model.addAttribute("insert", reportPath.toString());

      return "generic";

   }

   /**
    * Gets report.
    *
    * @param providergroupName the providergroup name
    * @param model             the model
    * @return the report
    */
   @GetMapping("/{providergroupName}")
   public String getReport(@RequestHeader("Accept") String acceptHeader, @PathVariable(value = "providergroupName") String providergroupName, Model model) {

      if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("application/json")) {

         return "forward:/download/linkchecker/" + providergroupName + "?format=json";
      }
      if(StringUtils.isNotEmpty(acceptHeader) && (acceptHeader.startsWith("application/xml") || acceptHeader.startsWith("text/xml"))) {

         return "forward:/download/linkchecker/" + providergroupName + "?format=xml";
      }
      if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("text/tab-separated-values")) {

         return "forward:/download/linkchecker/" + providergroupName + "?format=tsv";
      }

      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("linkchecker")
            .resolve(providergroupName + ".html");

      model.addAttribute("insert", reportPath.toString());

      return "generic";
   }

   /**
    * Gets latest checks.
    *
    * @param model the model
    * @return the latest checks
    */
   @GetMapping("/latestChecks")
   public String getLatestChecks(Model model) {

      Path objectFilePath = conf.getDirectory().getShare().resolve("latestChecks.obj");

      if (Files.notExists(objectFilePath)) {

         log.error("non existing object file '{}'", objectFilePath);
         throw new RuntimeException("file " + objectFilePath + " doesn't exist");
      }

      try (InputStream is = Files.newInputStream(objectFilePath)) {

         ObjectInputStream ois = new ObjectInputStream(is);

         @SuppressWarnings("unchecked")
         ArrayDeque<Map<String, String[]>> queue = (ArrayDeque<Map<String, String[]>>) ois.readObject();

         model.addAttribute("queue", queue);

      }
      catch (IOException ex) {
         
         log.error("can't read object file '{}'", objectFilePath);
         throw new RuntimeException();
      }
      catch (ClassNotFoundException e) {
         
         log.error("can't cast object file '{}'", objectFilePath);
         throw new RuntimeException();
      }

      model.addAttribute("insert", "fragments/latestChecks.html");

      return "generic";
   }
}
