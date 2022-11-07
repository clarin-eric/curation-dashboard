package eu.clarin.cmdi.curation.api;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableJpaRepositories(basePackages = "eu.clarin.cmdi.cpa.repository")
@EntityScan(basePackages = "eu.clarin.cmdi.cpa.model")
@EnableCaching
@EnableConfigurationProperties
@ComponentScan("eu.clarin.cmdi")
@EnableAutoConfiguration
public class TestConfig {

}
