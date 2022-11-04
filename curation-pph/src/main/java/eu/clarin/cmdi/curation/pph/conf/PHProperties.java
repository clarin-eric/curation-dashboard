package eu.clarin.cmdi.curation.pph.conf;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "curation.pph-service")
@Data
public class PHProperties {
   
   private Path xsdCache;
   
   private String RestApi;
   
   private String query;
}
