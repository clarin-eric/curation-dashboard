package eu.clarin.cmdi.curation.processor;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.*;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class CMDInstanceProcessor {
    private static final Logger _logger = LoggerFactory.getLogger(CMDInstanceProcessor.class);

    public CMDInstanceReport process(CMDInstance record, String parentName) {

        try {

            long start = System.currentTimeMillis();

            CMDInstanceReport report = new CMDInstanceReport();

            _logger.info("Started report generation for record: " + record.getPath());

            FileSizeValidator fileSizeValidator = new FileSizeValidator();
            fileSizeValidator.process(record, report);

            InstanceHeaderProcessor instanceHeaderProcessor = new InstanceHeaderProcessor();
            instanceHeaderProcessor.process(record, report);

            ResourceProxyProcessor resourceProxyProcessor = new ResourceProxyProcessor();
            resourceProxyProcessor.process(record, report);

            URLValidator urlValidator = new URLValidator();
            urlValidator.process(record, report, parentName);
            report.addSegmentScore(urlValidator.calculateScore(report));

            XMLValidator xmlValidator = new XMLValidator();
            xmlValidator.process(record, report);
            report.addSegmentScore(xmlValidator.calculateValidityScore());

            if (Configuration.COLLECTION_MODE) {
                CollectionInstanceFacetProcessor collectionInstanceFacetProcessor = new CollectionInstanceFacetProcessor();
                collectionInstanceFacetProcessor.process(record, report);
            } else {
                InstanceFacetProcessor instanceFacetProcessor = new InstanceFacetProcessor();
                instanceFacetProcessor.process(record, report);
            }

            long end = System.currentTimeMillis();
            _logger.info("It took " + TimeUtils.humanizeToTime(end - start) + "to generate the report for collection: " + report.getName());

            return report;
        }catch (Exception e){
            //todo delete this
            _logger.error("wtf:");
            e.printStackTrace();
            return null;
        }
    }

}
