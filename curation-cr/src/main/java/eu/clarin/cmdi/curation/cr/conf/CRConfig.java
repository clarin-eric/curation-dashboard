package eu.clarin.cmdi.curation.cr.conf;

import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "curation.cr-service")
@Data
public class CRConfig {

   private String restApi;

   private String query;
}
