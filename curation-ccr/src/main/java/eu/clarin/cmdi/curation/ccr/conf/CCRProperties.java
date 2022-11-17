package eu.clarin.cmdi.curation.ccr.conf;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "curation.ccr-service")
@Data
public class CCRProperties {
   
   private String restApi;
   
   private String query;
   
   private boolean enableTimer;

}
