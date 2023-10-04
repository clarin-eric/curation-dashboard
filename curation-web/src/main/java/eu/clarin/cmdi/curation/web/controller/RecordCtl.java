/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Controller
@RequestMapping("/record")
public class RecordCtl {
   
   @Autowired
   ApiConfig conf;
   
   @GetMapping("/**")
   public ResponseEntity<StreamingResponseBody> getFile(HttpServletRequest request) {
      
      log.debug("requestURL: {}", request.getRequestURL().toString());
      String origin = request.getRequestURL().toString().replaceFirst(".*/record/", "");
      log.debug("origin: {}", origin);
      log.debug("dataRoot: {}", conf.getDirectory().getDataRoot());
      Path cmdiPath = conf.getDirectory().getDataRoot()
            .resolve(origin).normalize();
      log.debug("cmdiPath: {}", cmdiPath);
      
      if(Files.notExists(cmdiPath) || !cmdiPath.startsWith(conf.getDirectory().getDataRoot())) {
         
         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "entity not found");         
      }
      
      StreamingResponseBody stream = outputStream -> {
         
         try (InputStream in = Files.newInputStream(cmdiPath)) {

            int b = -1;

            while ((b = in.read()) != -1) {
               outputStream.write(b);
            }

         }
         catch (IOException e) {
            
            log.error("can't read CMD instance '{}'", cmdiPath);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);              
         }      
      };
      
      return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(stream);
   }
}
