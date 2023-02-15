package eu.clarin.cmdi.curation.web;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan({"eu.clarin.cmdi.curation", "eu.clarin.linkchecker.persistence"})
@EnableJpaRepositories(basePackages = "eu.clarin.linkchecker.persistence.repository")
@EntityScan(basePackages = "eu.clarin.linkchecker.persistence.model")
public class CurationWebApplication {
   
   @Autowired
   TemplateEngine templateEngine;

	public static void main(String[] args) {
		SpringApplication.run(CurationWebApplication.class, args);
	}
	
   @PostConstruct
   public void init() {
      FileTemplateResolver ftr = new FileTemplateResolver();
      ftr.setCheckExistence(true); //just to be sure in case the ftr is used first
      ftr.setCacheable(false);
      
      templateEngine.addTemplateResolver(ftr);
   }

}
