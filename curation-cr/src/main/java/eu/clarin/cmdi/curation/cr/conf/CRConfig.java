package eu.clarin.cmdi.curation.cr.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "curation.cr-service")
@Data
public class CRConfig {
   
   private Path xsdCache;
}
