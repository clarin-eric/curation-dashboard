package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.entities.Collection;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.utils.TimeUtils;

/**
 * @author dostojic
 *
 */
public class CollectionAggregator extends ProcessingStep<Collection, CollectionReport> {

    private static final Logger _logger = LoggerFactory.getLogger(CollectionAggregator.class);

    private final int CHUNK_SIZE = 10000;

    @Override
    public boolean process(Collection dir, final CollectionReport report) {

	report.provider = dir.getPath().getFileName().toString();

	report.size = dir.getSize();
	report.maxFileSize = dir.getMaxFileSize();
	report.minFileSize = dir.getMinFileSize();

	report.numOfFiles = dir.getNumOfFiles();
	
	// process in portions to avoid memory thresholds
	Iterator<CurationEntity> it = dir.getChildren().iterator();
	int processed = 0;
	while (it.hasNext()) {
	    final List<CurationEntity> chunk = new ArrayList<>(CHUNK_SIZE);
	    for (int i = 0; i < CHUNK_SIZE && it.hasNext(); i++) {
		chunk.add(it.next());
		it.remove();
	    }
	    
	    long startTime = System.currentTimeMillis();
	    chunk.parallelStream().forEach(CurationEntity::generateReport);
	    long end = System.currentTimeMillis();
	    _logger.info("validation for {} files lasted {}", chunk.size(), TimeUtils.humanizeTime(end - startTime));	    	    
	    chunk.stream().forEach(child -> child.getReport().mergeWithParent(report));

	    processed += chunk.size();
	    _logger.debug("{} records are processed so far, rest {}", processed, dir.getChildren().size());

	}

	// ResProxies
	report.percOfValidFiles = 1.0 * report.numOfValidFiles / report.numOfFiles;
	report.avgNumOfResProxies = 1.0 * report.totNumOfResProxies / report.numOfFiles;
	report.avgNumOfResWithMime = 1.0 * report.totNumOfResWithMime / report.numOfFiles;
	report.avgNumOfLandingPages = 1.0 * report.totNumOfLandingPages / report.numOfFiles;
	report.avgNumOfLandingPagesWithoutLink = 1.0 * report.totNumOfLandingPagesWithoutLink / report.numOfFiles;
	report.avgNumOfResources = 1.0 * report.totNumOfResources / report.numOfFiles;
	report.avgNumOfMetadata = 1.0 * report.totNumOfMetadata / report.numOfFiles;

	// XMLValidator
	report.avgNumOfXMLElements = 1.0 * report.totNumOfXMLElements / report.numOfFiles;
	report.avgNumOfXMLSimpleElements = 1.0 * report.totNumOfXMLSimpleElements / report.numOfFiles;
	report.avgXMLEmptyElement = 1.0 * report.totNumOfXMLEmptyElement / report.numOfFiles;

	report.avgRateOfPopulatedElements = 1.0 * (report.totNumOfXMLSimpleElements - report.totNumOfXMLEmptyElement)
		/ report.numOfFiles;

	// URL
	report.avgNumOfLinks = 1.0 * report.totNumOfLinks / report.numOfFiles;
	report.avgNumOfUniqueLinks = 1.0 * report.totNumOfUniqueLinks / report.numOfFiles;
	report.avgNumOfResProxiesLinks = 1.0 * report.totNumOfResProxiesLinks / report.numOfFiles;
	report.avgNumOfBrokenLinks = 1.0 * report.totNumOfBrokenLinks / report.numOfFiles;

	report.avgPercOfValidLinks = 1.0 * (report.totNumOfLinks - report.totNumOfBrokenLinks) / report.numOfFiles;

	// Facets
	report.avgFacetCoverageByInstance = 1.0 * report.avgFacetCoverageByInstanceSum / report.numOfFiles;
	
	if(!CMDIInstance.duplicateMDSelfLinks.isEmpty())
	    report.duplicatedMDSelfLink = new ArrayList(CMDIInstance.duplicateMDSelfLinks);
	CMDIInstance.duplicateMDSelfLinks.clear();
	CMDIInstance.uniqueMDSelfLinks.clear();

	return true;

    }
}
