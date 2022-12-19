package eu.clarin.cmdi.curation.api;

import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.CollectionReport;
import eu.clarin.cmdi.curation.api.report.LinkcheckerDetailReport;
import eu.clarin.cmdi.curation.api.report.LinkcheckerDetailReport.CategoryReport;
import eu.clarin.cmdi.curation.api.report.LinkcheckerDetailReport.StatusDetailReport;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.pph.conf.PPHConfig;
import eu.clarin.linkchecker.persistence.model.StatusDetail;
import eu.clarin.linkchecker.persistence.repository.StatusDetailRepository;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import javax.transaction.Transactional;

@Service
@Slf4j
public class CurationModuleImpl implements CurationModule {

   @Autowired
   private PPHConfig pphConf;
   @Autowired
   private ApplicationContext ctx;
   @Autowired
   private StatusDetailRepository sdRep;

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

   @Override
   @Transactional
   public Collection<LinkcheckerDetailReport> getLinkcheckerDetailReports() {
      
      final Collection<LinkcheckerDetailReport> linkcheckerDetailReports = new ArrayList<LinkcheckerDetailReport>();
      
      final LinkcheckerDetailReport[] lastLinkcheckerDetailReport = new LinkcheckerDetailReport[1];
      final CategoryReport[] lastCategoryReport = new CategoryReport[1];

      
      try(Stream<StatusDetail> stream = sdRep.findByOrderNrLessThanEqual(100l)){
         
         stream.forEach(statusDetail -> {
            
            if(lastLinkcheckerDetailReport[0] == null || !lastLinkcheckerDetailReport[0].getName().equals(statusDetail.getProvidergroupname())) {
               
               lastLinkcheckerDetailReport[0] = new LinkcheckerDetailReport(statusDetail.getProvidergroupname());
               
               linkcheckerDetailReports.add(lastLinkcheckerDetailReport[0]);
            
            }
            
            if(lastCategoryReport[0] == null || lastCategoryReport[0].getCategory() != statusDetail.getCategory()) {
               
               lastCategoryReport[0] = new CategoryReport(statusDetail.getCategory());
               
               lastLinkcheckerDetailReport[0].getCategoryReports().add(lastCategoryReport[0]);
               
            }
            
            lastCategoryReport[0].getStatusDetails().add(
                  new StatusDetailReport(
                     statusDetail.getUrlname(),
                     statusDetail.getOrigin(),
                     statusDetail.getMethod(), 
                     statusDetail.getStatusCode(),
                     statusDetail.getMessage(), 
                     statusDetail.getCheckingDate(), 
                     statusDetail.getContentType(), 
                     statusDetail.getExpectedMimeType(),
                     statusDetail.getContentLength(), 
                     statusDetail.getDuration(), 
                     statusDetail.getRedirectCount()
                  )
               );
         });         
      }
      
      
      return linkcheckerDetailReports;
   
   }
}
