/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.app.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;

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
   
   private int linkDeactivationAfter;
   
   private int linkDeletionAfter;
   
   private int purgeHistoryAfter;
   
   private int purgeObsoleteAfter;

   private int purgeReportAfter;
   
   @Data
   public static class Directory {
      
      private Path home;
      
      private Path dataRoot;
      
      private Collection<Path> in;
      
      private Path out;
      
   }
}
