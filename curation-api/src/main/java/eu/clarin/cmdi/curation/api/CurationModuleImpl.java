package eu.clarin.cmdi.curation.api;

import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport.CategoryReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport.Context;
import eu.clarin.cmdi.curation.api.report.linkchecker.LinkcheckerDetailReport.StatusDetailReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.linkchecker.persistence.model.AggregatedStatus;
import eu.clarin.linkchecker.persistence.model.Status;
import eu.clarin.linkchecker.persistence.repository.AggregatedStatusRepository;
import eu.clarin.linkchecker.persistence.repository.StatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * The type Curation module.
 */
@Service
@Slf4j
public class CurationModuleImpl implements CurationModule {

   @Autowired
   private ApplicationContext ctx;
   @Autowired
   private AggregatedStatusRepository asRep;
   @Autowired
   private StatusRepository sRep;

   /**
    * Process cmd profile cmd profile report.
    *
    * @param schemaLocation the schema location
    * @return the cmd profile report
    */
   @Override
   public CMDProfileReport processCMDProfile(String schemaLocation) {
       try {
           return ctx.getBean(CMDProfile.class, schemaLocation, true).generateReport();
       }
       catch (MalFunctioningProcessorException e) {
           throw new RuntimeException(e);
       }
   }

   /**
    * Process cmd profile cmd profile report.
    *
    * @param path the path
    * @return the cmd profile report
    */
   @Override
   public CMDProfileReport processCMDProfile(Path path) {
         
         return processCMDProfile(path.toUri().toString());
   }

   /**
    * Process cmd instance cmd instance report.
    *
    * @param path the path
    * @return the cmd instance report
    */
   @Override
   public CMDInstanceReport processCMDInstance(Path path) {
      if (Files.exists(path)) {
         try {
            return ctx.getBean(CMDInstance.class, path, Files.size(path), "testProvider").generateReport();
         }
         catch (IOException | BeansException | MalFunctioningProcessorException e) {

            throw new RuntimeException(e);

         }
      }

      else {

         log.error("path '{}' does not exist", path);
         throw new RuntimeException();

      }
   }

   /**
    * Process cmd instance cmd instance report.
    *
    * @param url the url
    * @return the cmd instance report
    */
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
      catch (IOException | MalFunctioningProcessorException e) {

         throw new RuntimeException(e);

      }
   }

   /**
    * Process collection collection report.
    *
    * @param path the path
    * @return the collection report
    */
   @Override
   public CollectionReport processCollection(Path path) throws MalFunctioningProcessorException {

      return ctx.getBean(CMDCollection.class, path).generateReport();

   }

   /**
    * Gets linkchecker detail reports.
    *
    * @return the linkchecker detail reports
    */
   @Override
   @Transactional
   public Collection<LinkcheckerDetailReport> getLinkcheckerDetailReports() {
      
      final Collection<LinkcheckerDetailReport> linkcheckerDetailReports = new ArrayList<LinkcheckerDetailReport>();
      final LinkcheckerDetailReport overallReport = new LinkcheckerDetailReport("Overall");
      linkcheckerDetailReports.add(overallReport);
      
      final LinkcheckerDetailReport[] lastLinkcheckerDetailReport = new LinkcheckerDetailReport[1];
      final CategoryReport[] lastCategoryReport = new CategoryReport[1];

      
      try(Stream<AggregatedStatus> aStream = StreamSupport.stream(asRep.findAll().spliterator(), false)){
         
         aStream.forEach(aStatus -> {
            
            try(Stream<Status> sStream = sRep.findAllByProvidergroupAndCategory(aStatus.getProvidergroupName(), aStatus.getCategory())){
               
               sStream.limit(31).forEach(status -> {
                  
                  StatusDetailReport statusDetailReport =  new StatusDetailReport(
                        status.getUrl().getName(),
                        status.getMethod(), 
                        status.getStatusCode(),
                        status.getMessage(), 
                        status.getCheckingDate(), 
                        status.getContentType(), 
                        status.getContentLength(), 
                        status.getDuration(), 
                        status.getRedirectCount()
                     );
                  
                  status.getUrl().getUrlContexts().forEach(uc -> statusDetailReport.getContexts().add(new Context(uc.getContext().getOrigin(), uc.getExpectedMimeType())));
 
                  
                  if(lastLinkcheckerDetailReport[0] == null || !lastLinkcheckerDetailReport[0].getName().equals(aStatus.getProvidergroupName())) {
                     
                     lastLinkcheckerDetailReport[0] = new LinkcheckerDetailReport(aStatus.getProvidergroupName());
                     lastCategoryReport[0] = null;
                     
                     linkcheckerDetailReports.add(lastLinkcheckerDetailReport[0]);                  
                  }
                  
                  if(lastCategoryReport[0] == null || lastCategoryReport[0].getCategory() != aStatus.getCategory()) {
                     
                     lastCategoryReport[0] = new CategoryReport(aStatus.getCategory());
                     
                     lastLinkcheckerDetailReport[0].getCategoryReports().add(lastCategoryReport[0]);
                     
                     overallReport.getCategoryReports()
                        .stream()
                        .filter(report -> report.getCategory() == aStatus.getCategory())
                        .findFirst()
                        .orElseGet(() -> {
                           CategoryReport categoryReport = new CategoryReport(aStatus.getCategory());
                           overallReport.getCategoryReports().add(categoryReport);
                           
                           return categoryReport;
                        })
                        .getStatusDetails()
                        .add(statusDetailReport);
                  }
                  
                  lastCategoryReport[0].getStatusDetails().add(statusDetailReport);                        
               });              
            }            
         });         
      }      
      
      return linkcheckerDetailReports;  
   }
}
