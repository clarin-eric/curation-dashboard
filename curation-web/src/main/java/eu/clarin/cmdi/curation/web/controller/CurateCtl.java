package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.api.CurationModule;
import eu.clarin.cmdi.curation.api.entity.CurationEntityType;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.api.utils.FileStorage;
import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.curation.web.conf.WebConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
   @Autowired
   HttpUtils httpUtils;

   /**
    * Gets instance query param.
    *
    * @param urlStr the url str
    * @param model  the model
    * @return the instance query param
    */
   @GetMapping()
   public String getInstanceQueryParam(@RequestHeader("Accept") String acceptHeader, @RequestParam(name="url-input", required=false) String urlStr, Model model) {
      
      log.debug("urlStr: {}", urlStr);

      if (urlStr == null || urlStr.isEmpty()) {
         
         model.addAttribute("insert", "fragments/validator.html");

         return "generic";
      }
      else {
         
         Path inFilePath = null;
         
         
         // just to make it more user friendly ignore leading and trailing spaces
         urlStr = urlStr.trim();

         if (urlStr.startsWith("www")) {
            urlStr = "http://" + urlStr;
         }         
         
         if (urlStr.matches("http(s)?://.+")) { //the string is a URL

            try {

               if (crService.isPublicSchema(urlStr)) { // is a public schema URL

                  if (StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("application/json")) {

                     return "forward:/download/profile/" + FileNameEncoder.encode(urlStr) + "?format=json";
                  }
                  else if (StringUtils.isNotEmpty(acceptHeader) && (acceptHeader.startsWith("application/xml") || acceptHeader.startsWith("text/xml"))){

                     return "forward:/download/profile/" + FileNameEncoder.encode(urlStr);
                  }
                  else {

                     return "forward:/profile/" + FileNameEncoder.encode(urlStr) + ".html";
                  }
               }
               else {

                  try (InputStream in = httpUtils.getURLConnection(urlStr, MediaType.APPLICATION_XML_VALUE).getInputStream()) {

                     inFilePath = Files.createTempFile(FileNameEncoder.encode(urlStr), "xml");

                     FileUtils.copyInputStreamToFile(in, inFilePath.toFile());
                  }
                  catch (IOException e) {

                     log.error("couldn't download URL '{}' to file '{}'", urlStr, inFilePath);
                     throw new RuntimeException("internal error - please inform Clarin-Eric");

                  }
               }
            }
            catch (PPHCacheException e){

               throw new RuntimeException("internal error - please inform Clarin-Eric");
            }
         }
         else {//the string is a relative path

            inFilePath = conf.getDirectory().getDataRoot().resolve(urlStr).normalize();
            log.debug("inFilePath: {}", inFilePath);
            if (Files.notExists(inFilePath) || !inFilePath.startsWith(conf.getDirectory().getDataRoot())) {
               
               log.error("no valid input file with path '{}'", inFilePath);
               throw new RuntimeException("Given URL is invalid");
            } // else go down and curate the file
         }

         String returnString = createReport(inFilePath, acceptHeader, model);
         return returnString;
         

      }
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
      }
      catch (IOException e) {
         
         log.error("can't access multipart file '{}'", file);
         throw new RuntimeException("Given URL is invalid");
      }
      
      return createReport(inFilePath, null, model);
   }

   private String createReport(Path inFilePath, String acceptHeader, Model model) {

      NamedReport report = null;     
      
      byte[] buffer = new byte[200];
      
      try(InputStream inStream = Files.newInputStream(inFilePath)){
         
         inStream.read(buffer);
         
         String text = new String(buffer);
         
         if (text.contains("xmlns:xs=")) {// it's a profile


               report = curation.processCMDProfile(inFilePath.toUri().toString());
         }
         else { // no profile - so processed as CMD instance
            
            report = curation.processCMDInstance(inFilePath);
         }

         // we're saving any user upload as instance to prevent 
         // overriding public profiles by user intervention
         storage.saveReportAsXML(report, CurationEntityType.INSTANCE, false);

         storage.saveReportAsHTML(report, CurationEntityType.INSTANCE, false);

         if(StringUtils.isNotEmpty(acceptHeader) && acceptHeader.startsWith("application/json")) {

            return "forward:/download/instance/" + report.getName() + "?format=json";
         }
         else if(StringUtils.isNotEmpty(acceptHeader) && (acceptHeader.startsWith("application/xml") || acceptHeader.startsWith("text/xml"))) {

            return "forward:/download/instance/" + report.getName();
         }
         else {

            Path htmlFilePath = conf.getDirectory().getOut()
                    .resolve("html")
                    .resolve("instance")
                    .resolve(report.getName() + ".html");

            model.addAttribute("insert", htmlFilePath.toString());
         }

         return "generic";

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
