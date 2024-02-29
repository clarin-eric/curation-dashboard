package eu.clarin.cmdi.curation.pph.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "curation.pph-service")
@Data
public class PPHConfig {
   
   private Path xsdCache;
   
   private String RestApi;
   
   private String query;
}
