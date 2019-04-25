package eu.clarin.helpers;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class FileManager {

    private static final Logger _logger = Logger.getLogger(FileManager.class);

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static BufferedImage readImage(String filePath) throws IOException {
        return ImageIO.read(new File(filePath));
    }

    public static void writeToFile(String path, String content) throws IOException {
        File file = new File(path);
        file.createNewFile();
        file.setReadable(true,false);

        try (PrintStream out = new PrintStream(new FileOutputStream(path))) {
            out.print(content);
            out.flush();
        }

    }
}
