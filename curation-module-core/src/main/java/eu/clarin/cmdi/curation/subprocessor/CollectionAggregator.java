package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.facets.FacetConceptMappingService;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.report.CollectionReport.FacetCollectionStruct;
import eu.clarin.cmdi.curation.report.CollectionReport.FacetReport;
import eu.clarin.cmdi.curation.report.CollectionReport.FileReport;
import eu.clarin.cmdi.curation.report.CollectionReport.HeaderReport;
import eu.clarin.cmdi.curation.report.CollectionReport.ResProxyReport;
import eu.clarin.cmdi.curation.report.CollectionReport.URLValidationReport;
import eu.clarin.cmdi.curation.report.CollectionReport.XMLValidationReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.utils.TimeUtils;

/**
 * @author dostojic
 *
 */
public class CollectionAggregator extends ProcessingStep<CMDCollection, CollectionReport> {

	private static final Logger _logger = LoggerFactory.getLogger(CollectionAggregator.class);

	private final int CHUNK_SIZE = 10000;

	@Override
	public void process(CMDCollection dir, final CollectionReport report) {
		
		report.fileReport = new FileReport();
		report.headerReport = new HeaderReport();
		report.resProxyReport = new ResProxyReport();
		report.xmlReport = new XMLValidationReport();
		report.urlReport = new URLValidationReport();
		report.facetReport = new FacetReport();
		
		report.facetReport.facet = new ArrayList<>();
		new FacetConceptMappingService().getFacetNames().forEach(f -> {
			FacetCollectionStruct facet = new FacetCollectionStruct();
			facet.name = f;
			facet.cnt = 0;
			report.facetReport.facet.add(facet);
		});
		
		
		//add info regarding file statistics
		report.fileReport.provider = dir.getPath().getFileName().toString();
		report.fileReport.numOfFiles = dir.getNumOfFiles();
		report.fileReport.size = dir.getSize();
		report.fileReport.minFileSize = dir.getMinFileSize();
		report.fileReport.maxFileSize = dir.getMaxFileSize();
		

		// process in portions to avoid memory thresholds
		Iterator<CurationEntity> it = dir.getChildren().iterator();
		int processed = 0;
		while (it.hasNext()) {
			final List<CurationEntity> chunk = new ArrayList<>(CHUNK_SIZE);
			for (int i = 0; i < CHUNK_SIZE && it.hasNext(); i++) {
				chunk.add(it.next());
			}

			long startTime = System.currentTimeMillis();
			chunk.parallelStream().forEach(CurationEntity::generateReport);
			long end = System.currentTimeMillis();
			_logger.info("validation for {} files lasted {}", chunk.size(), TimeUtils.humanizeTime(end - startTime));
			chunk.stream().forEach(child -> child.generateReport().mergeWithParent(report));

			processed += chunk.size();
			_logger.debug("{} records are processed so far, rest {}", processed, dir.getChildren().size() - processed);

		}

		report.calculateAverageValues();

		if (!CMDInstance.duplicateMDSelfLink.isEmpty()){
			report.headerReport.duplicatedMDSelfLink = CMDInstance.duplicateMDSelfLink;
		}
		CMDInstance.duplicateMDSelfLink.clear();
		CMDInstance.mdSelfLinks.clear();

	}

	@Override
	public Score calculateScore(CollectionReport report) {
		double score = report.fileReport.numOfFiles;
		if(report.invalidFiles != null){
			report.invalidFiles.forEach(ir -> addMessage(Severity.ERROR, ir));
			score = (score - report.invalidFiles.size()) / score;
		}
		
		return new Score(score, (double) report.fileReport.numOfFiles, "invalid-files", msgs);
	}
	
}
