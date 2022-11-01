package eu.clarin.cmdi.curation.processor;

import eu.clarin.cmdi.curation.configuration.CurationConfig;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.exception.SubprocessorException;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.*;

import org.springframework.beans.factory.annotation.Autowired;


public class CMDInstanceProcessor {
   
   @Autowired
   private CurationConfig conf;

    public CMDInstanceReport process(CMDInstance record, String parentName) throws SubprocessorException {

        CMDInstanceReport report = new CMDInstanceReport();

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

        return report;
    }

}
