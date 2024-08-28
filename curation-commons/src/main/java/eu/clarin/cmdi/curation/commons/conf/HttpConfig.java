package eu.clarin.cmdi.curation.commons.conf;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "curation.http")
@Data
@Slf4j
public class HttpConfig {

    private String userAgent;

    private int connectionTimeout;

    private int readTimeout;

    private String proxyHost;

    private int proxyPort;

}
