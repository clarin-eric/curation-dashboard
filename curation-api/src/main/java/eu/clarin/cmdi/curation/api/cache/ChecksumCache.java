package eu.clarin.cmdi.curation.api.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.Adler32;

@Slf4j
@Component
public class ChecksumCache {

    @Cacheable(value = "checksumCache", key ="#dir.toString()")
    public long getChecksum(Path dir) {
        return -1;
    }

    @CachePut(value = "checksumCache", key = "#dir.toString()")
    public long getNewChecksum(Path dir) {
        Adler32 adler32 = new Adler32();
        try(Stream<Path> stream = Files.walk(dir)) {
            stream
            .filter(Files::isRegularFile)
            .sorted(Comparator.comparing(Path::toString))
            .forEach(path -> {
                // Include file contents
                try (InputStream is = Files.newInputStream(path)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        adler32.update(buffer, 0, read);
                    }
                } catch (IOException e) {
                    log.error("can't read file '{}'", path, e);
                    throw new RuntimeException(e);
                }
            });
        }
        catch (IOException e) {
            log.error("can't create checksum from directory '{}'", dir, e);
        }
        return adler32.getValue();
    }
}
