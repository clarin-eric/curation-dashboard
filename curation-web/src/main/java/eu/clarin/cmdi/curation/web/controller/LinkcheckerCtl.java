/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Stack;

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
@RequestMapping("/linkchecker")
@Slf4j
public class LinkcheckerCtl {

   @Autowired
   WebConfig conf;

   @GetMapping()
   public String getReport(Model model) {

      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("linkchecker")
            .resolve("AllLinkcheckerReport.html");

      log.debug("reportPath: {}", reportPath);

      model.addAttribute("insert", reportPath.toString());

      return "generic";

   }

   @GetMapping("/{providergroupName}")
   public String getReport(@PathVariable(value = "providergroupName") String providergroupName, Model model) {

      Path reportPath = conf.getDirectory().getOut().resolve("html").resolve("linkchecker")
            .resolve(providergroupName + ".html");

      model.addAttribute("insert", reportPath.toString());

      return "generic";
   }

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
         Stack<Map<String, String[]>> stack = (Stack<Map<String, String[]>>) ois.readObject();

         model.addAttribute("stack", stack);

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
