/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.web.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "curation")
public class WebConfig {

   private Directory directory = new Directory();
   
   private LocalDateTime startTime = LocalDateTime.now();
   
   private String docUrl;
   
   @Data
   public static class Directory {
      
      private Path home;
      
      private Path dataRoot;
      
      private Collection<Path> in;
      
      private Path out;
      
      private Path share;
      
   }
}
