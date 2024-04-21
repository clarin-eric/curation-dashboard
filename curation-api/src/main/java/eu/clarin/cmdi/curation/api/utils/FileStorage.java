/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 */
package eu.clarin.cmdi.curation.api.utils;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CurationEntityType;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.util.JAXBSource;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.BasicTransformerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type File storage.
 */
@Slf4j
@Component
public class FileStorage {

    private final Pattern creationTimePattern = Pattern.compile("creationTime=\"(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\"");

    private final Pattern previousCreationTimePattern = Pattern.compile("previousCreationTime=\"(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\"");

    /**
     * The Conf.
     */
    private final ApiConfig conf;

    public FileStorage(ApiConfig conf) {
        this.conf = conf;
    }

    /**
     * Save report.
     *
     * @param report          the report
     * @param entityType      the entity type
     * @param makeStampedCopy the make stamped copy
     */
    public void saveReport(NamedReport report, CurationEntityType entityType, boolean makeStampedCopy) {

        //getting creation time from previous report with simple pattern search and set it as previous creation time in current report
        if (makeStampedCopy) {

            Path previousReportPath = getOutputPath(report.getName(), entityType, "xml");

            if (Files.exists(previousReportPath)) {

                try (InputStream in = Files.newInputStream(previousReportPath)) {

                    byte[] buffer = new byte[500];
                    in.read(buffer);

                    String text = new String(buffer);

                    Matcher matcher = this.creationTimePattern.matcher(text);

                    LocalDateTime previousCreationTime = null;

                    // the second conditions assures that we don't point to the same file in case we run the program more than one time per day
                    if (matcher.find() && !report.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE).equals(matcher.group(1).substring(0, 10))) {

                        previousCreationTime = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } else {

                        matcher = this.previousCreationTimePattern.matcher(text);

                        if (matcher.find()) {

                            previousCreationTime = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        }
                    }

                    report.setPreviousCreationTime(previousCreationTime);
                }
                catch (IOException e) {

                    log.error("couldn't get previous creation time from file {}", previousReportPath);
                }
            }
        }

        saveReportAsXML(report, entityType, makeStampedCopy);

        saveReportAsHTML(report, entityType, makeStampedCopy);
    }

    /**
     * Check output path.
     */
    @PostConstruct
    public void checkOutputPath() {

        if (conf.getDirectory().getOut() == null && Files.notExists(conf.getDirectory().getOut())) {

            log.error("the property 'curation.directory.out' is either not set or the path doesn't exist");
            throw new RuntimeException();

        }
    }


    /**
     * Save report as xml path.
     *
     * @param report     the report
     * @param entityType the entity type
     * @return the path
     */
    public Path saveReportAsXML(NamedReport report, CurationEntityType entityType, boolean makeStampedCopy) {

        Path outputPath = getOutputPath(report.getName(), entityType, "xml");

        Marshaller marshaller = null;

        try {
            JAXBContext jc = JAXBContext.newInstance(report.getClass());

            marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
        }
        catch (PropertyException e) {

            log.error("can't set properties for marshaller");
            throw new RuntimeException(e);

        }
        catch (JAXBException e) {

            log.error("can't create JAXBContext for class '{}'\n{}", report.getClass(), e);

        }

        try {
            marshaller.marshal(report, Files.newOutputStream(outputPath));
        }
        catch (IOException e) {

            log.error("can't create/write to file '{}'", outputPath);
            throw new RuntimeException(e);
        }
        catch (JAXBException e) {

            log.error("can't marshall report '{}'", report.getName());
        }

        if(makeStampedCopy){

            String stampedFileName = report.getName() + "_" + report.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".xml";

            Path stampedPath = outputPath.getParent().resolve(stampedFileName);

            try {
                Files.copy(outputPath, stampedPath, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {

                log.error("can't copy file '{}' to stamped file '{}'", outputPath, stampedPath);
                throw new RuntimeException(e);
            }
        }

        return outputPath;
    }

    /**
     * Save report as html path.
     *
     * @param report     the report
     * @param entityType the entity type
     * @return the path
     */
    public Path saveReportAsHTML(NamedReport report, CurationEntityType entityType, boolean makeStampedCopy) {

        Path outputPath = getOutputPath(report.getName(), entityType, "html");

        TransformerFactory factory = BasicTransformerFactory.newInstance();

        String resourceName = "/xslt/" + report.getClass().getSimpleName() + "2HTML.xsl";

        Source xslt = new StreamSource(
                this.getClass().getResourceAsStream(resourceName));

        Transformer transformer;

        try {
            transformer = factory.newTransformer(xslt);

            transformer.transform(new JAXBSource(JAXBContext.newInstance(report.getClass()), report),
                    new StreamResult(outputPath.toFile()));
        }
        catch (TransformerConfigurationException e) {

            log.error("can't load resource '{}'", resourceName);
            throw new RuntimeException(e);

        }
        catch (TransformerException | JAXBException e) {

            log.error("can't transform report '{}'", report.getName());

        }

        if(makeStampedCopy){

            String stampedFileName = report.getName() + "_" + report.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".html";

            Path stampedPath = outputPath.getParent().resolve(stampedFileName);

            resourceName = "/xslt/" + report.getClass().getSimpleName() + "2HTMLStamped.xsl";

            InputStream in = this.getClass().getResourceAsStream(resourceName);

            if(in != null){

                xslt = new StreamSource(in);

                try {
                    transformer = factory.newTransformer(xslt);

                    transformer.transform(new JAXBSource(JAXBContext.newInstance(report.getClass()), report),
                            new StreamResult(stampedPath.toFile()));
                }
                catch (TransformerConfigurationException e) {

                    log.error("can't load resource '{}'", resourceName);
                    throw new RuntimeException(e);

                }
                catch (TransformerException | JAXBException e) {

                    log.error("can't transform report '{}'", report.getName());

                }
            }
            else{

                try {
                    Files.copy(outputPath, stampedPath, StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e) {

                    log.error("can't copy file '{}' to stamped file '{}'", outputPath, stampedPath);
                    throw new RuntimeException(e);
                }
            }
        }

        return outputPath;
    }

    private Path getOutputPath(String reportName, CurationEntityType entityType, String fileSuffix) {
        Path outPath = conf.getDirectory().getOut().resolve(fileSuffix).resolve(entityType.toString());

        if (Files.notExists(outPath)) {
            try {
                Files.createDirectories(outPath);
            }
            catch (IOException e) {

                log.error("can't create directory '{}' - make sure the user has got creation rights", outPath);
                throw new RuntimeException(e);

            }
        }

        String filename = FileNameEncoder.encode(reportName) + "." + fileSuffix;

        return outPath.resolve(filename);
    }
}
