package eu.clarin.cmdi.curation.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class CurationWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurationWebApplication.class, args);
	}

}
