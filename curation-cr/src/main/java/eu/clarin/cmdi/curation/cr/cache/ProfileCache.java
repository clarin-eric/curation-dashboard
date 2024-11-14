package eu.clarin.cmdi.curation.cr.cache;

import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@Slf4j
public class ProfileCache {

    private final HttpUtils httpUtils;

    public ProfileCache(HttpUtils httpUtils) {

        this.httpUtils = httpUtils;
    }

    @Cacheable(value = "profileCache", key = "#schemaLocation")
    public String getProfileAsString(String schemaLocation) {

        String fileName = schemaLocation.replaceAll("[/.:]", "_");

        Path xsd = Paths.get("/tmp/curation/cr_cache").resolve(fileName + ".xsd");

        // if not download it

        try {
            if (Files.notExists(xsd) || Files.size(xsd) == 0) {

                if(Files.notExists(Paths.get("/tmp/curation/cr_cache"))) {

                    try {

                        Files.createDirectories(Paths.get("/tmp/curation/cr_cache"));
                    }
                    catch (IOException e) {

                        log.error("could create cr cache directory '{}'", "/tmp/curation/cr_cache");
                    }
                }

                log.debug("XSD for the {} is not in the local cache, it will be downloaded", schemaLocation);

                try(InputStream in = httpUtils.getURLConnection(schemaLocation).getInputStream()) {

                    Files.copy(in, xsd, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        catch (MalformedURLException e) {

            log.debug("the schema location URL '{}' is malformed", schemaLocation);
            return null;
        }
        catch (IOException e) {

            log.debug("can't access xsd file '{}'", schemaLocation);
            return null;
        }

        String schemaString;

        try(BufferedReader reader = Files.newBufferedReader(xsd)) {

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
