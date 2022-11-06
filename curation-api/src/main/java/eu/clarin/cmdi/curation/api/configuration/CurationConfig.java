package eu.clarin.cmdi.curation.api.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "curation")
@Data
@Slf4j
public class CurationConfig {
   
   private String mode;
   
   private int maxFileSize;
   
   private Collection<String> facets;
   
   private Directory directory = new Directory();
   
   private int threadPoolSize;
   
   private String vloConfigLocation;
   
   private String linkDataSource;
   
   private String crQuery;
   
   @Bean
   public VloConfig vloConfig() {
      try {
         if (vloConfigLocation == null || vloConfigLocation.isEmpty()) {
            log.warn(
                  "loading default VloConfig.xml from vlo-commons.jar - PROGRAM WILL WORK BUT WILL PROBABABLY DELIVER UNATTENDED RESULTS!!!");
            log.warn("make sure to define a valid curation:vlo_config_location in the file application.yml");
            return new DefaultVloConfigFactory().newConfig();
         }
         else {
            log.info("loading VloConfig.xml from location {}", vloConfigLocation);
            return new XmlVloConfigFactory(new File(vloConfigLocation).toURI().toURL())
                  .newConfig();
         }
      }
      catch(IOException ex) {
         log.error("could create instance of VloConfig");
         return null;
      }
      
   }
   
   
   
   @Data
   public static class Directory {
      
      private Path home;
      
      private Path reports;
      
      private Path data;
      
      private Path xsdCache;
   }

}
