package eu.clarin.cmdi.curation.cr.cache;

import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
@Slf4j
public class ProfileCache {

    private final HttpUtils httpUtils;

    public ProfileCache(HttpUtils httpUtils) {

        this.httpUtils = httpUtils;
    }

    @Cacheable(value = "profileCache", key = "#schemaLocation", sync = true)
    public String getProfileAsString(String schemaLocation) {

        String schemaString;

        try {

            schemaString = httpUtils.getString(new URI(schemaLocation));
        }
        catch (IOException | URISyntaxException | InterruptedException e) {

            log.error("couldn't get schema '{}'", schemaLocation);
            return null;
        }

        return schemaString;
    }
}
