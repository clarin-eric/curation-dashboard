package eu.clarin.cmdi.curation.api.utils;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.zip.Adler32;

/**
 * The type Dir checksum.
 */
@Slf4j
@Component
@Lazy
public class DirChecksum {

    private static final String OBJ_FILE_NAME = "checksum.obj";

    private final Path objFilePath;

    private HashMap<String, Long> checksums;


    public DirChecksum(ApiConfig apiConfig) {

        this.objFilePath = apiConfig.getDirectory().getShare().resolve(OBJ_FILE_NAME);

        if(!Files.exists(objFilePath)){
            this.checksums = new HashMap<>();
        }
        else{
            try(ObjectInputStream in = new ObjectInputStream(Files.newInputStream(objFilePath))){

                this.checksums = (HashMap<String, Long>) in.readObject();
            }
            catch (ClassNotFoundException | IOException e) {
                log.error("can't read file '{}'", objFilePath, e);
                this.checksums = new HashMap<>();
            }
        }
    }

    @PreDestroy
    private void destroy(){

        try(OutputStream out = Files.newOutputStream(this.objFilePath)){
            new ObjectOutputStream(out).writeObject(this.checksums);
        }
        catch (IOException e) {
            log.error("can't write file '{}'", objFilePath, e);
        }
    }

    public boolean hasChanged(Path dir) {

        boolean hasChanged = true;

        Adler32 adler32 = new Adler32();
        try {
            Files.walk(dir)
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
            // the directory content has either changed when there is no previous checksum
            // or when new and previous checksum are different
            hasChanged = !this.checksums.containsKey(dir.toString()) || this.checksums.get(dir.toString()).longValue() != adler32.getValue();

            this.checksums.put(dir.toString(), adler32.getValue());
        }
        catch (IOException e) {
            log.error("can't create checksum from directory '{}'", dir, e);
        }

        return hasChanged;
    }
}
