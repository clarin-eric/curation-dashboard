package eu.clarin.cmdi.curation.subprocessor;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.*;
import eu.clarin.cmdi.curation.report.CollectionReport.*;
import eu.clarin.cmdi.curation.report.CollectionReport.FacetReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
public class CollectionAggregator {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionAggregator.class);

    protected Collection<Message> msgs = null;

    public void process(CMDCollection collection, CollectionReport report) {

        report.fileReport = new FileReport();
        report.headerReport = new HeaderReport();
        report.resProxyReport = new ResProxyReport();
        report.xmlPopulatedReport = new XMLPopulatedReport();
        report.xmlValidationReport = new XMLValidationReport();
        report.urlReport = new URLValidationReport();
        report.facetReport = new FacetReport();

        report.facetReport.facet = new ArrayList<>();

        for (String facetName : Configuration.FACETS) {
            FacetCollectionStruct facet = new FacetCollectionStruct();
            facet.name = facetName;
            facet.cnt = 0;
            report.facetReport.facet.add(facet);
        }

        //add info regarding file statistics
        report.fileReport.provider = collection.getPath().getFileName().toString();
        report.fileReport.numOfFiles = collection.getNumOfFiles();
        report.fileReport.size = collection.getSize();
        report.fileReport.minFileSize = collection.getMinFileSize();
        report.fileReport.maxFileSize = collection.getMaxFileSize();

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Configuration.THREADPOOL_SIZE);

        while (!collection.getChildren().isEmpty()) {

            CMDInstance instance = collection.getChildren().pop();
            executor.submit(() ->
            {
                try {
                    CMDInstanceReport cmdInstanceReport = instance.generateReport(report.getName());
                    cmdInstanceReport.mergeWithParent(report);
                } 
                catch(TransformerException | FileSizeException | SAXException | VTDException e) {
                    LOG.info("Error while generating report for instance: " + instance.getPath() + ":" + e.getMessage()+ " Skipping to next instance...");
                    new ErrorReport(instance.getPath().toString(), e.getMessage()).mergeWithParent(report);
                }
                catch (IOException | ExecutionException | ParserConfigurationException e) {
                    LOG.error("Error while generating report for instance: " + instance.getPath() + ":" + e.getMessage()+ " Skipping to next instance...");
                    new ErrorReport(instance.getPath().toString(), e.getMessage()).mergeWithParent(report);
                }
            });
        }

        executor.shutdown();

        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOG.error("Error occured while waiting for the threadpool to terminate.");
            }
        }


        report.calculateAverageValues();

        if (!CMDInstance.duplicateMDSelfLink.isEmpty()) {
            report.headerReport.duplicatedMDSelfLink = CMDInstance.duplicateMDSelfLink;
        }
        CMDInstance.duplicateMDSelfLink.clear();
        CMDInstance.mdSelfLinks.clear();

    }

    public Score calculateScore(CollectionReport report) {
        double score = report.fileReport.numOfFiles;
        if (report.file != null) {
            report.file.forEach(ir -> addMessage(Severity.ERROR, "Invalid file:" + ir.recordName + ", reason: " + ir.reason));
            score = (score - report.file.size()) / score;
        }

        return new Score(score, (double) report.fileReport.numOfFiles, "invalid-files", msgs);
    }

    protected void addMessage(Severity lvl, String message) {
        if (msgs == null) {
            msgs = new ArrayList<>();
        }
        msgs.add(new Message(lvl, message));
    }

}
