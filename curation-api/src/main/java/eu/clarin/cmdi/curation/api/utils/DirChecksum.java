package eu.clarin.cmdi.curation.api.utils;

import eu.clarin.cmdi.curation.api.cache.ChecksumCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * The idea of class DirCheckum is get the information if the content of a given directory has changed since it was
 * accessed last time. To do so we calculate a checksum based on the Adler32 algorithm for the directory
 * and compare it with the previously calculated checksum for the directory.
 * All checksums are stored in a HashMap which is persisted when the instance of DirChecksum is destroyed and
 * re-instantiated, when the instance of DirChecksum is created.
 */
@Slf4j
@Component
@Lazy
public class DirChecksum {

    private final ChecksumCache  checksumCache;

    /**
     * Instantiates a new Dir checksum.
     *
     * @param checksumCache the checksum cache which allows to compare current and previous checksum
     */
    public DirChecksum(ChecksumCache checksumCache) {
        this.checksumCache = checksumCache;
    }


    /**
     * Has changed boolean.
     *
     * @param dir the directory which we want to analyze for changes
     * @return the boolean if the directory content has changed or not
     */
    public boolean hasChanged(Path dir) {
        // the directory content has either changed when there is no previous checksum
        // or when new and previous checksum are different
        return this.checksumCache.getChecksum(dir) == 0 || this.checksumCache.getChecksum(dir) != this.checksumCache.getNewChecksum(dir);
    }
}
