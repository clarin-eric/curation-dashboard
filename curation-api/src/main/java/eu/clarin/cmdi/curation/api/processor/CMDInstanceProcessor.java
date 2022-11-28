package eu.clarin.cmdi.curation.api.processor;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.subprocessor.instance.*;
import lombok.extern.slf4j.Slf4j;

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

   public CMDInstanceReport process(CMDInstance record) {

      CMDInstanceReport report = new CMDInstanceReport();

      try {
         fileSizeValidator.process(record, report);
         report.addSegmentScore(fileSizeValidator.calculateScore(report));

         instanceHeaderProcessor.process(record, report);
         report.addSegmentScore(instanceHeaderProcessor.calculateScore(report));

         resourceProxyProcessor.process(record, report);
         report.addSegmentScore(resourceProxyProcessor.calculateScore(report));

         urlValidator.process(record, report);
         report.addSegmentScore(urlValidator.calculateScore(report));

         xmlValidator.process(record, report);
         report.addSegmentScore(xmlValidator.calculateScore(report));

         if ("collection".equalsIgnoreCase(conf.getMode())) {
            collectionInstanceFacetProcessor.process(record, report);
            report.addSegmentScore(collectionInstanceFacetProcessor.calculateScore(report));
         }
         else {
            instanceFacetProcessor.process(record, report);
            report.addSegmentScore(instanceFacetProcessor.calculateScore(report));
         }
      }
      catch (SubprocessorException e) {
         log.debug("can't process file '{}'", record.getPath());
      }

      return report;
   }
}
