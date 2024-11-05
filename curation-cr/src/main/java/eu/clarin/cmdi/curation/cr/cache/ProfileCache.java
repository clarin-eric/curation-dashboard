package eu.clarin.cmdi.curation.cr.cache;

import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
@Slf4j
public class ProfileCache {

    private final HttpUtils httpUtils;

    public ProfileCache(HttpUtils httpUtils) {

        this.httpUtils = httpUtils;
    }

    @Cacheable(key = "#schemaLocation", sync = true)
    public String getProfileAsString(String schemaLocation) {

        String schemaString;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(httpUtils.getURLConnection(schemaLocation).getInputStream()))) {

            StringBuffer buffer = new StringBuffer();

            reader.lines().forEach(buffer::append);
            schemaString = buffer.toString();
        }
        catch (IOException e) {

            log.error("couldn't get schema '{}'", schemaLocation);
            return null;
        }

        return schemaString;
    }
}
