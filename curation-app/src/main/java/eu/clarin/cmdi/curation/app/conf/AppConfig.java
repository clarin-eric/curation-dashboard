/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.app.conf;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 *
 */
@Component
@ConfigurationProperties(prefix = "curation")
@Data
public class AppConfig {
   
   private String mode;
   
   private Directory directory = new Directory();
   
   private LocalDateTime startTime = LocalDateTime.now();
   
   private int linkDeaktivationAfter;
   
   private int linkDeletionAfter;
   
   @Data
   public static class Directory {
      
      private Path home;
      
      private Path dataRoot;
      
      private Collection<Path> in;
      
      private Path out;
      
   }
}
