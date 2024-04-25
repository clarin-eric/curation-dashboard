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

    private String userAgent = "CLARIN-curation-dashboard";

    private int connectionTimeout = 5000;

    private int readTimeout = 5000;

    private String proxyHost;

    private int proxyPort;

}
