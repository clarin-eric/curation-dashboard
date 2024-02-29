package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.api.CurationModule;
import eu.clarin.cmdi.curation.api.entity.CurationEntityType;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.api.utils.FileStorage;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.web.conf.WebConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The type Curate ctl.
 */
@Slf4j
@Controller
@RequestMapping(value = {"/", "/curate"})
public class CurateCtl {

   /**
    * The Conf.
    */
   @Autowired
   WebConfig conf;
   /**
    * The Curation.
    */
   @Autowired
   CurationModule curation;
   /**
    * The Storage.
    */
   @Autowired
   FileStorage storage;
   /**
    * The Cr service.
    */
   @Autowired
   CRService crService;

   /**
    * Gets instance query param.
    *
    * @param urlStr the url str
    * @param model  the model
    * @return the instance query param
    */
   @GetMapping()
   public String getInstanceQueryParam(@RequestParam(name="url-input", required=false) String urlStr, Model model) {  
      
      log.debug("urlStr: {}", urlStr);

      if (urlStr == null || urlStr.isEmpty()) {
         
         model.addAttribute("insert", "fragments/validator.html");
      }
      else {
         
         Path htmlFilePath = null;
         
         
         // just to make it more user friendly ignore leading and trailing spaces
         urlStr = urlStr.trim();

         if (urlStr.startsWith("www")) {
            urlStr = "http://" + urlStr;
         }         
         
         if (urlStr.matches("http(s)?://.+")) { //the string is a URL
            
            URL schemaURL;
            
            try {
               
               schemaURL = new URL(urlStr);
            }
            catch(MalformedURLException ex) {
               
               throw new RuntimeException("the URL '" + urlStr + "' is not valid");                  
            }   
               
            
            if(crService.isSchemaCRResident(urlStr)) { // is a public schema URL
                  
               NamedReport report = curation.processCMDProfile(schemaURL);
               
               // we're saving any user upload as instance to prevent 
               // overriding public profiles by user intervention
               storage.saveReportAsXML(report, CurationEntityType.INSTANCE);

               htmlFilePath = storage.saveReportAsHTML(report, CurationEntityType.INSTANCE);
            }
            else {  

               Path inFilePath = null;
               
               try {
                  inFilePath = Files.createTempFile(FileNameEncoder.encode(urlStr.toString()), "xml");
   
                  FileUtils.copyURLToFile(schemaURL, inFilePath.toFile());
               }
               catch (IOException e) {              
                  
                  log.error("couldn't download URL '{}' to file '{}'", urlStr, inFilePath);
                  throw new RuntimeException("internal error - please inform Clarin-Eric");
               
               }
               
               htmlFilePath = curate(inFilePath);
            }
         }
         else {//the string is a relative path

            Path inFilePath = conf.getDirectory().getDataRoot().resolve(urlStr).normalize();
            log.debug("inFilePath: {}", inFilePath);
            if (Files.notExists(inFilePath) || !inFilePath.startsWith(conf.getDirectory().getDataRoot())) {
               
               log.error("no valid input file with path '{}'", inFilePath);
               throw new RuntimeException("Given URL is invalid");
            } // else go down and curate the file
            
            htmlFilePath = curate(inFilePath);
         }
         
         model.addAttribute("insert", htmlFilePath.toString());               
      }
      
      return "generic";
   }

   /**
    * Post instance string.
    *
    * @param file  the file
    * @param model the model
    * @return the string
    */
// this is for drag and drop instance form
   @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public String postInstance(@RequestParam("file") MultipartFile file, Model model) {

      Path inFilePath = Paths.get(System.getProperty("java.io.tmpdir"), System.currentTimeMillis() + "_curation.tmp");
      
      try {
         file.transferTo(inFilePath);
         
         Path reportPath = curate(inFilePath);
         
         log.debug("reportPath: {}", reportPath);
         
         model.addAttribute("insert", reportPath.toString());

      }
      catch (IOException e) {
         
         log.error("can't access multipart file '{}'", file);
         throw new RuntimeException("Given URL is invalid");
      }
      
      return "generic";
   }

   private Path curate(Path inFilePath) {

      NamedReport report = null;     
      
      byte[] buffer = new byte[200];
      
      try(InputStream inStream = Files.newInputStream(inFilePath)){
         
         inStream.read(buffer);
         
         String text = new String(buffer);
         
         if (text.contains("xmlns:xs=")) {// it's a profile
            if (!text.contains("http://www.clarin.eu/cmd/1")) { // but not a valid cmd 1.2 profile
               throw new RuntimeException("profile has no cmd 1.2 namespace declaration");
            }
            else {
               
               try {
                  report = curation.processCMDProfile(inFilePath.toUri().toURL());
               }
               catch (MalformedURLException e) {
                  
                 new RuntimeException(e);
               }   
            }
         }
         else { // no profile - so processed as CMD instance
            
            report = curation.processCMDInstance(inFilePath);
         }

         // we're saving any user upload as instance to prevent 
         // overriding public profiles by user intervention
         storage.saveReportAsXML(report, CurationEntityType.INSTANCE);

         return storage.saveReportAsHTML(report, CurationEntityType.INSTANCE);

      }
      catch (IOException e) {

         log.error("can't read file '{}'", inFilePath);
         throw new RuntimeException("internal error - please inform Clarin-Eric");     
      }
   }

   /**
    * Gets robots txt.
    *
    * @return the robots txt
    */
   @GetMapping("/robots.txt")
   public ResponseEntity<String> getRobotsTxt() {     
      return ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(
               """
                  User-agent: *
                  Disallow: /download
                  Disallow: /record
                  
                  User-agent: CLARIN-Linkchecker
                  Allow: /
                  """
               );
   }
}
