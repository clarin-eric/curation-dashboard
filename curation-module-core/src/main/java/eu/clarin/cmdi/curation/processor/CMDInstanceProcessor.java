package eu.clarin.cmdi.curation.processor;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.*;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class CMDInstanceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CMDInstanceProcessor.class);

    public CMDInstanceReport process(CMDInstance record, String parentName) throws FileSizeException, TransformerException, IOException, ExecutionException, ParserConfigurationException, SAXException, VTDException {

        CMDInstanceReport report = new CMDInstanceReport();

//        logger.info("Started report generation for record: " + record.getPath());
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


        if (Configuration.COLLECTION_MODE) {
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
