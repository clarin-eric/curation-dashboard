package eu.clarin.helpers;

import eu.clarin.cmdi.curation.report.Report;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

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
        file.setReadable(true, false);

        try (PrintStream out = new PrintStream(new FileOutputStream(path))) {
            out.print(content);
            out.flush();
        }

    }

    public static String marshall(Report report) throws JAXBException {

        JAXBContext jc = JAXBContext.newInstance(report.getClass());

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");

        StringWriter sw = new StringWriter();
        marshaller.marshal(report, sw);

        return sw.toString();
    }

    public static String readInputStream(InputStream fileInputStream) {
        //read inputstream into string (from stackoverflow, like all code ever)
        return new BufferedReader(new InputStreamReader(fileInputStream))
                .lines().collect(Collectors.joining("\n"));
    }
}