package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.api.CurationModule;
import eu.clarin.cmdi.curation.api.entity.CurationEntityType;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.api.utils.FileStorage;
import eu.clarin.cmdi.curation.web.conf.WebConfig;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Controller
@RequestMapping(value = {"", "/curate"})
public class CurateCtl {
   
   @Autowired
   WebConfig conf;
   @Autowired
   CurationModule curation;
   @Autowired
   FileStorage storage;

   @GetMapping()
   public String getInstanceQueryParam(@RequestParam(name="url-input", required=false) String urlStr, Model model) {      

      if (urlStr == null || urlStr.isEmpty()) {
         model.addAttribute("insert", "fragments/validator.html");
      }
      else {
         
         Path inFilePath = null;
         
         // just to make it more user friendly ignore leading and trailing spaces
         urlStr = urlStr.trim();


         if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
            if (urlStr.startsWith("www")) {
               urlStr = "http://" + urlStr;
            }   

            try {
               inFilePath = Files.createTempFile(FileNameEncoder.encode(urlStr.toString()), "xml");

               FileUtils.copyURLToFile(new URL(urlStr), inFilePath.toFile());
            }
            catch (MalformedURLException e) {
               
               throw new RuntimeException("the URL '" + urlStr + "' is not valid");
               
            }
            catch (IOException e) {              
               
               log.error("couldn't download URL '{}' to file '{}'", urlStr, inFilePath);
               throw new RuntimeException("internal error - please inform Clarin-Eric");
            
            }
         }
         else {
            
            inFilePath = conf.getDirectory().getDataRoot().resolve(urlStr);// here it is regarded as a path instead of url.

            if (Files.notExists(inFilePath)) {
               
               log.error("no valid unput file with path '{}'", inFilePath);
               throw new RuntimeException("Given URL is invalid");
            } // else go down and curate the file
         }
         
         Path htmlFilePath = curate(inFilePath);
         
         model.addAttribute("include", htmlFilePath.toString());       
         
      }
      return "generic";
   }
   
   // this is for drag and drop instance form
   @PostMapping(path = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public String postInstance(@RequestParam("file") MultipartFile file, Model model) {

      Path inFilePath;
      try {
         inFilePath = file.getResource().getFile().toPath();
         Path htmlFilePath = curate(inFilePath);
         
         model.addAttribute("include", htmlFilePath.toString());
      }
      catch (IOException e) {
         
         log.error("can't access multipart file '{}'", file);
         throw new RuntimeException("Given URL is invalid");
      }
      
      return "generic";
   }

   private Path curate(Path inFilePath) {

      NamedReport report;
      
      CurationEntityType entityType;
      
      StringBuffer buffer = new StringBuffer();
      
      try(BufferedReader reader = Files.newBufferedReader(inFilePath)){
         String line = null; 
         
         while((line = reader.readLine()) != null && buffer.length() < 200) {
            
            buffer.append(line.subSequence(0, 200));
         }
      }
      catch (IOException e) {

         log.error("can't read file '{}'", inFilePath);
         throw new RuntimeException("internal error - please inform Clarin-Eric");
         
      }
      

      if (buffer.toString().contains("xmlns:xs=")) {// it's a profile
         if (!buffer.toString().contains("http://www.clarin.eu/cmd/1")) { // but not a valid cmd 1.2 profile
            throw new RuntimeException("profile has no cmd 1.2 namespace declaration");
         }
         else {
            
            entityType = CurationEntityType.PROFILE;
            report = curation.processCMDInstance(inFilePath);
         
         }
      }
      else { // no profile - so processed as CMD instance
         entityType = CurationEntityType.INSTANCE;
         report = curation.processCMDInstance(inFilePath);
      }

      storage.saveReportAsXML(report, entityType);

      return storage.saveReportAsHTML(report, entityType);
   }
}
