package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.utils.TimeUtils;

/**
 * @author dostojic
 *
 */
public class CollectionAggregator extends ProcessingStep<CMDCollection, CollectionReport> {

	private static final Logger _logger = LoggerFactory.getLogger(CollectionAggregator.class);

	private final int CHUNK_SIZE = 10000;

	@Override
	public boolean process(CMDCollection dir, final CollectionReport report) {

		report.addFileReport(dir.getPath().getFileName().toString(), dir.getNumOfFiles(), dir.getSize(),
				dir.getMinFileSize(), dir.getMaxFileSize());

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

		if (!CMDInstance.duplicateMDSelfLinks.isEmpty())
			report.addSelfLinks(new ArrayList(CMDInstance.duplicateMDSelfLinks));
		CMDInstance.duplicateMDSelfLinks.clear();
		CMDInstance.uniqueMDSelfLinks.clear();

		return true;

	}
}
