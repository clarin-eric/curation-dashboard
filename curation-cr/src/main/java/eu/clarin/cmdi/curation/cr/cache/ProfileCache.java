package eu.clarin.cmdi.curation.cr.cache;

import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Slf4j
public class ProfileCache {

    private final HttpUtils httpUtils;

    public ProfileCache(HttpUtils httpUtils) {

        this.httpUtils = httpUtils;
    }

    @Cacheable(value = "profileCache", key = "#schemaLocation", condition = "!#schemaLocation.startsWith('file:')", sync = true)
    public String getProfileAsString(String schemaLocation) {

        try {
            if(schemaLocation.startsWith("file:")){

                return Files.readString(Paths.get(new URI(schemaLocation)));
            }

            return httpUtils.getString(new URI(schemaLocation));
        }
        catch (IOException | URISyntaxException | InterruptedException | IllegalArgumentException e) {

            log.error("couldn't get schema '{}'", schemaLocation);
            return null;
        }
    }
}
