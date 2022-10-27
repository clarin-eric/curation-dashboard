package eu.clarin.cmdi.curation.main;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.exception.ProfileNotFoundException;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.utils.FileNameEncoder;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.curation.utils.FileDownloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

@Service
public class CurationModuleImpl implements CurationModule {

   @Autowired
   private VloConfig vloConf;

   @Override public Report<?> processCMDProfile(String profileId)
         throws ExecutionException, ProfileNotFoundException, IOException {
      return new CMDProfile(vloConf.getComponentRegistryProfileSchema(profileId), "1.x")
            .generateReport();
   }

   @Override
   public Report<?> processCMDProfile(URL schemaLocation)
         throws ExecutionException, ProfileNotFoundException, IOException {
      return new CMDProfile(schemaLocation.toString(), "1.x").generateReport();
   }

   @Override
   public Report<?> processCMDProfile(Path path) throws IOException, ExecutionException, ProfileNotFoundException {

      return processCMDProfile(path.toUri().toURL());
   }

   @Override
   public Report<?> processCMDInstance(Path path) throws IOException, FileSizeException, ExecutionException,
         TransformerException, SAXException, VTDException, ParserConfigurationException {
      if (Files.notExists(path))
         throw new IOException(path.toString() + " doesn't exist!");

      return new CMDInstance(path, Files.size(path)).generateReport(null);

   }

   @Override
   public Report<?> processCMDInstance(URL url) throws IOException, FileSizeException, ExecutionException,
         TransformerException, SAXException, VTDException, ParserConfigurationException {
      String path = FileNameEncoder.encode(url.toString()) + ".xml";
      Path cmdiFilePath = Paths.get(System.getProperty("java.io.tmpdir"), path);
      new FileDownloader(15000).download(url.toString(), cmdiFilePath.toFile());
      long size = Files.size(cmdiFilePath);
      CMDInstance cmdInstance = new CMDInstance(cmdiFilePath, size);
      cmdInstance.setUrl(url.toString());

      CMDInstanceReport report = cmdInstance.generateReport(null);

//		Files.delete(path);

      report.fileReport.location = url.toString();

      return report;
   }

   @Override
   public Report<?> processCollection(Path path) throws IOException {

      return null;
   }

   @Override
   public Report<?> aggregateReports(Collection<Report> reports) {

      return null;
   }
}
