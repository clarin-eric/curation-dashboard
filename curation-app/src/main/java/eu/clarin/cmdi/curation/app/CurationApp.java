package eu.clarin.cmdi.curation.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "eu.clarin.cmdi.cpa.repository")
@EntityScan(basePackages = "eu.clarin.cmdi.cpa.model")
@EnableCaching
@EnableConfigurationProperties
@ComponentScan("eu.clarin.cmdi")
@EnableAutoConfiguration
public class CurationApp {

	public static void main(String[] args) {
		SpringApplication.run(CurationApp.class, args);
	}
	
   @Bean
   public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
      return args -> {

         

      };
   }	
}
