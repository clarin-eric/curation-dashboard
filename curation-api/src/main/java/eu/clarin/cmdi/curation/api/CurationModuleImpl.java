package eu.clarin.cmdi.curation.api;

import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.ErrorReport;
import eu.clarin.cmdi.curation.api.report.Report;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.pph.conf.PPHConfig;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

@Service
@Slf4j
public class CurationModuleImpl implements CurationModule {

   @Autowired//
   private PPHConfig pphConf;
   @Autowired
   private ApplicationContext ctx;

   @Override public Report<?> processCMDProfile(String profileId) throws MalformedURLException, SubprocessorException {
      return processCMDProfile(new URL(pphConf.getRestApi() + "/" + profileId + "/xsd"));
   }

   @Override
   public Report<?> processCMDProfile(URL schemaLocation) throws SubprocessorException {
      return ctx.getBean(CMDProfile.class, schemaLocation.toString(), "1.x").generateReport();
   }

   @Override
   public Report<?> processCMDProfile(Path path) throws MalformedURLException, SubprocessorException {

      return processCMDProfile(path.toUri().toURL());
   }

   @Override
   public Report<?> processCMDInstance(Path path) {
      if (Files.exists(path)) {
            try {
               return ctx.getBean(CMDInstance.class, path, Files.size(path)).generateReport(null);
            }
            catch (SubprocessorException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
      }
     
      return new ErrorReport(); 

   }

   @Override
   public Report<?> processCMDInstance(URL url) {
      String path = FileNameEncoder.encode(url.toString()) + ".xml";
      Path cmdiFilePath = Paths.get(System.getProperty("java.io.tmpdir"), path);
      
      try{
      
         FileUtils.copyURLToFile(url, cmdiFilePath.toFile());
   
         long size = Files.size(cmdiFilePath);
         
         CMDInstance cmdInstance = ctx.getBean(CMDInstance.class, cmdiFilePath, size);
         cmdInstance.setUrl(url.toString());
   
         CMDInstanceReport report = cmdInstance.generateReport(null);
   
   //		Files.delete(path);
   
         report.fileReport.location = url.toString();
   
         return report;
      }
      catch(SubprocessorException e) {
         
         //TODO
         
      }
      catch (IOException e) {
         
         // TODO Auto-generated catch block
         
      }
      
      return new ErrorReport();
   }

   @Override
   public Report<?> processCollection(Path path) {
      
      return ctx.getBean(CMDCollection.class, path).generateReport();

   }

   @Override
   public Report<?> aggregateReports(Collection<Report<?>> reports) {

      return null;
   }
}
