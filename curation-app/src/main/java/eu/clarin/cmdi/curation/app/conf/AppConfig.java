/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.app.conf;

import java.nio.file.Path;
import java.util.Collection;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

/**
 *
 */
@ConfigurationProperties(prefix = "curation")
@Data
public class AppConfig {
   
   private Directory directory = new Directory();
   
   @Data
   public static class Directory {
      
      private Path home;
      
      private Path dataRoot;
      
      private Collection<Path> in;
      
      private Path out;
      
   }
}
