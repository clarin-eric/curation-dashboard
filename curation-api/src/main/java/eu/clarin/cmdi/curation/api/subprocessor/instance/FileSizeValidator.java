package eu.clarin.cmdi.curation.api.subprocessor.instance;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.FileReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type File size validator.
 */
@Slf4j
@Component
public class FileSizeValidator extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   private static final Pattern _pattern = Pattern.compile("xmlns(:.+?)?=\"http(s)?://www.clarin.eu/cmd/(1)?");

   private final ApiConfig conf;

   /**
    * Instantiates a new File size validator.
    *
    * @param conf the conf
    */
   public FileSizeValidator(ApiConfig conf) {

      this.conf = conf;
   }

   private boolean isLatestVersion(Path path) {
      String line = null;
      Matcher matcher;

      try (BufferedReader reader = Files.newBufferedReader(path)) {

         while ((line = reader.readLine()) != null)
            if ((matcher = _pattern.matcher(line)).find())
               return matcher.group(3) != null;
      }
      catch (IOException ex) {
         log.error("can't identitfy cmdi version by namespace", ex);
      }
      return false;
   }

   /**
    * Process.
    *
    * @param instance the instance
    * @param report   the report
    */
   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) {

      report.fileReport = new FileReport();

      // convert cmdi 1.1 to 1.2 if necessary
      if ("instance".equalsIgnoreCase(conf.getMode()) && !isLatestVersion(instance.getPath())) {
         Path newPath = null;

         try {
            newPath = Files.createTempFile(null, ".xml");
         }
         catch (IOException e) {

            log.error("can't create temporary outputfile for CMD1.1 to CMD1.x transformation");
            throw new RuntimeException(e);

         }

         TransformerFactory factory = TransformerFactory.newInstance();
         Source xslt = new StreamSource(this.getClass().getResourceAsStream("/xslt/cmd-record-1_1-to-1_2.xsl"));

         Transformer transformer;
         try {
            transformer = factory.newTransformer(xslt);
            transformer.transform(new StreamSource(instance.getPath().toFile()), new StreamResult(newPath.toFile()));
         }
         catch (TransformerConfigurationException e) {

            log.error(
                  "can't create Transformer object from resource '/xslt/cmd-record-1_1-to-1_2.xsl' - make sure that the resource is in the classpath!");
            throw new RuntimeException(e);
         }
         catch (TransformerException e) {

            log.debug("can't transfrom input file '{}'", instance.getPath());

            report.details.add(new Detail(Severity.FATAL, "file",
                  "can't transform input file '" + instance.getPath().getFileName() + "'"));
            report.isProcessable = false;

            return;

         }

         report.details.add(new Detail(Severity.INFO, "file", "tranformed cmdi version 1.1 into version 1.2"));

         instance.setPath(newPath);
         try {
            instance.setSize(Files.size(newPath));
         }
         catch (IOException e) {

            log.error("can't get size from temporary transfromer output file '{}'", newPath);
            throw new RuntimeException(e);

         }
      }

      report.fileReport.size = instance.getSize();

      // from instance upload
      if (instance.getUrl() != null) {
         report.fileReport.location = FileNameEncoder.encode(instance.getUrl());
      }
      else {
         Path filePath = instance.getPath();

         // file in the data directory
         if (filePath.startsWith(conf.getDirectory().getDataRoot())) {
            report.fileReport.location = conf.getDirectory().getDataRoot().relativize(filePath).toString();
         }
         // otherwise
         else {
            report.fileReport.location = filePath.toString();
         }

      }

      if (report.fileReport.size > conf.getMaxFileSize().toBytes()) {

         log.debug("file '{}' has a size of {} bytes which exceeds the limit of {}", instance.getPath(),
               report.fileReport.size, conf.getMaxFileSize());

         report.details.add(new Detail(Severity.FATAL, "file",
               "the file size exceeds the limit allowed (" + conf.getMaxFileSize().toMegabytes() + "MB)"));
         report.isProcessable = false;

         return;

      }

      log.debug("...done");

      report.fileReport.score = 1.0;
      report.instanceScore += report.fileReport.score;
   }
}
