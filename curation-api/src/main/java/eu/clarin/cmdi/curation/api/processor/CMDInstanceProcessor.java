package eu.clarin.cmdi.curation.api.processor;

import eu.clarin.cmdi.curation.api.configuration.CurationConfig;
import eu.clarin.cmdi.curation.api.entities.CMDInstance;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.subprocessor.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class CMDInstanceProcessor {

   private CMDInstanceProcessor() {
      super();
   }

   @Autowired
   private CurationConfig conf;
   @Autowired
   private ApplicationContext ctx;

   public CMDInstanceReport process(CMDInstance record, String parentName) {

      CMDInstanceReport report = new CMDInstanceReport();

      try {
         FileSizeValidator fileSizeValidator = ctx.getBean(FileSizeValidator.class);
         fileSizeValidator.process(record, report);
         report.addSegmentScore(fileSizeValidator.calculateScore());

         InstanceHeaderProcessor instanceHeaderProcessor = ctx.getBean(InstanceHeaderProcessor.class);
         instanceHeaderProcessor.process(record, report);
         report.addSegmentScore(instanceHeaderProcessor.calculateScore(report));

         ResourceProxyProcessor resourceProxyProcessor = ctx.getBean(ResourceProxyProcessor.class);
         resourceProxyProcessor.process(record, report);
         report.addSegmentScore(resourceProxyProcessor.calculateScore(report));

         URLValidator urlValidator = ctx.getBean(URLValidator.class);
         urlValidator.process(record, report, parentName);
         report.addSegmentScore(urlValidator.calculateScore(report));

         XMLValidator xmlValidator = ctx.getBean(XMLValidator.class);
         xmlValidator.process(record, report);
         report.addSegmentScore(xmlValidator.calculateValidityScore());
         report.addSegmentScore(xmlValidator.calculateScore(report));

         if (conf.getMode().equalsIgnoreCase("collection")) {
            CollectionInstanceFacetProcessor collectionInstanceFacetProcessor = ctx
                  .getBean(CollectionInstanceFacetProcessor.class);
            collectionInstanceFacetProcessor.process(record, report);
            report.addSegmentScore(collectionInstanceFacetProcessor.calculateScore(report));

         }
         else {
            InstanceFacetProcessor instanceFacetProcessor = ctx.getBean(InstanceFacetProcessor.class);
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
