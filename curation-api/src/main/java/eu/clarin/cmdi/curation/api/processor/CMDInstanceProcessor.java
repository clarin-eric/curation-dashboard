package eu.clarin.cmdi.curation.api.processor;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.subprocessor.instance.*;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CMDInstanceProcessor {

   @Autowired
   private ApiConfig conf;
   @Autowired
   FileSizeValidator fileSizeValidator;
   @Autowired
   InstanceHeaderProcessor instanceHeaderProcessor;
   @Autowired
   ResourceProxyProcessor resourceProxyProcessor;
   @Autowired
   URLValidator urlValidator;
   @Autowired
   XMLValidator xmlValidator;
   @Autowired
   CollectionInstanceFacetProcessor collectionInstanceFacetProcessor;
   @Autowired
   InstanceFacetProcessor instanceFacetProcessor;
   

   public CMDInstanceReport process(CMDInstance record, String parentName) {

      CMDInstanceReport report = new CMDInstanceReport();


         
         Stream.of(
               fileSizeValidator, 
               instanceHeaderProcessor, 
               resourceProxyProcessor, 
               urlValidator, 
               xmlValidator, 
               "collection".equalsIgnoreCase(conf.getMode())?collectionInstanceFacetProcessor:instanceFacetProcessor
               ).forEach(subprocessor -> {
                  try {
                     subprocessor.process(record, report);
                     report.addSegmentScore(subprocessor.calculateScore(report));
                  }
                  catch (SubprocessorException e) {
                     log.debug("can't process file '{}'", record.getPath());
                  }
                  catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
               });

      return report;
   }
}
