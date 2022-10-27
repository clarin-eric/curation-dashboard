package eu.clarin.cmdi.curation.ph;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration   
@ComponentScan(basePackages = "eu.clarin.cmdi.curation")
@EnableCaching
public class TestConfig {

}
