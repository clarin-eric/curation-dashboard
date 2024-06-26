package eu.clarin.cmdi.curation.ccr.conf;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "curation.ccr-service")
@Data
public class CCRConfig {

   private Path ccrCache;
   
   private String restApi;
   
   private String query;

}
