package eu.clarin.cmdi.curation.api.processor;

import eu.clarin.cmdi.curation.api.configuration.CurationConfig;
import eu.clarin.cmdi.curation.api.entities.CMDInstance;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.subprocessor.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class CMDInstanceProcessor {
   
   @Autowired
   private CurationConfig conf;

    public CMDInstanceReport process(CMDInstance record, String parentName){

        CMDInstanceReport report = new CMDInstanceReport();

        try {
           FileSizeValidator fileSizeValidator = new FileSizeValidator();
           fileSizeValidator.process(record, report);
           report.addSegmentScore(fileSizeValidator.calculateScore());
   
           InstanceHeaderProcessor instanceHeaderProcessor = new InstanceHeaderProcessor();
           instanceHeaderProcessor.process(record, report);
           report.addSegmentScore(instanceHeaderProcessor.calculateScore(report));
   
           ResourceProxyProcessor resourceProxyProcessor = new ResourceProxyProcessor();
           resourceProxyProcessor.process(record, report);
           report.addSegmentScore(resourceProxyProcessor.calculateScore(report));
   
           URLValidator urlValidator = new URLValidator();
           urlValidator.process(record, report, parentName);
           report.addSegmentScore(urlValidator.calculateScore(report));
   
           XMLValidator xmlValidator = new XMLValidator();
           xmlValidator.process(record, report);
           report.addSegmentScore(xmlValidator.calculateValidityScore());
           report.addSegmentScore(xmlValidator.calculateScore(report));
   
   
           if (conf.getMode().equalsIgnoreCase("collection")) {
               CollectionInstanceFacetProcessor collectionInstanceFacetProcessor = new CollectionInstanceFacetProcessor();
               collectionInstanceFacetProcessor.process(record, report);
               report.addSegmentScore(collectionInstanceFacetProcessor.calculateScore(report));
   
           } else {
               InstanceFacetProcessor instanceFacetProcessor = new InstanceFacetProcessor();
               instanceFacetProcessor.process(record, report);
               report.addSegmentScore(instanceFacetProcessor.calculateScore(report));
           }
        }
        catch(SubprocessorException e) {
           
           log.debug("can't process file '{}'", record.getPath());
        }

        return report;
    }

}
