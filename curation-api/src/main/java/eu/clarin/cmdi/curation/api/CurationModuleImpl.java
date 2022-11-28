package eu.clarin.cmdi.curation.api;

import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.CollectionReport;
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

@Service
@Slf4j
public class CurationModuleImpl implements CurationModule {

   @Autowired
   private PPHConfig pphConf;
   @Autowired
   private ApplicationContext ctx;

   @Override
   public CMDProfileReport processCMDProfile(String profileId) {
      
      String urlStr = pphConf.getRestApi() + "/" + profileId + "/xsd";
      try {
         return processCMDProfile(new URL(pphConf.getRestApi() + "/" + profileId + "/xsd"));
      }
      catch (MalformedURLException e) {
         
         log.error("malformed URL '{}' - check if property 'curation.pph-service.restApi' is set correctly", urlStr);
         throw new RuntimeException(e);
      }
   
   }

   @Override
   public CMDProfileReport processCMDProfile(URL schemaLocation) {
      return ctx.getBean(CMDProfile.class, schemaLocation.toString(), "1.x").generateReport();
   }

   @Override
   public CMDProfileReport processCMDProfile(Path path) {

      try {
         
         return processCMDProfile(path.toUri().toURL());
      
      }
      catch (MalformedURLException e) {
         
         log.error("can't create URL from path '{}'", path);
         throw new RuntimeException(e);
      
      }
   }

   @Override
   public CMDInstanceReport processCMDInstance(Path path) {
      if (Files.exists(path)) {
         try {
            return ctx.getBean(CMDInstance.class, path, Files.size(path)).generateReport();
         }
         catch (IOException e) {

            throw new RuntimeException(e);

         }
      }

      else {

         log.error("path '{}' does not exist", path);
         throw new RuntimeException();

      }
   }

   @Override
   public CMDInstanceReport processCMDInstance(URL url) {

      try {
         
         Path cmdiFilePath = Files.createTempFile(FileNameEncoder.encode(url.toString()), "xml");

         FileUtils.copyURLToFile(url, cmdiFilePath.toFile());

         long size = Files.size(cmdiFilePath);

         CMDInstance cmdInstance = ctx.getBean(CMDInstance.class, cmdiFilePath, size);
         cmdInstance.setUrl(url.toString());

         CMDInstanceReport report = cmdInstance.generateReport();

         // Files.delete(path);

         report.fileReport.location = url.toString();

         return report;
      }
      catch (IOException e) {

         throw new RuntimeException(e);

      }
   }

   @Override
   public CollectionReport processCollection(Path path) {

      return ctx.getBean(CMDCollection.class, path).generateReport();

   }
}
