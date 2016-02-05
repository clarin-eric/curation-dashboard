package eu.clarin.cmdi.curation.subprocessor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.entities.Directory;
import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */
public class DirectoryAggregator implements ProcessingActivity<Directory> {

    private static final Logger _logger = LoggerFactory.getLogger(DirectoryAggregator.class);

    private final int CHUNK_SIZE = 10000;

    @Override
    public Severity process(Directory dir) {

	// use max num of cores
	ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

	StringBuilder childReport = new StringBuilder();

	// process in portions to avoid memory thresholds
	Iterator<CurationEntity> it = dir.getChildren().iterator();
	int processed = 0;
	while (it.hasNext()) {
	    final Collection<CurationEntity> chunk = new ArrayList<>(CHUNK_SIZE);
	    for (int i = 0; i < CHUNK_SIZE && it.hasNext(); i++) {
		chunk.add(it.next());
		it.remove(); //kill the stupid kids
	    }

	    try {
		long numOfValid = (long) forkJoinPool.submit(() -> chunk.parallelStream().mapToLong(CurationEntity::genReport).sum()).get();
		dir.setNumOfValidFiles(dir.getNumOfValidFiles() + numOfValid);
		if (Config.PRINT_COLLECTION_DETAILS) {
		    chunk.forEach(child -> childReport.append("\n------------------------------------------------\n")
			    .append(child));
		}

		processed += chunk.size();
		_logger.debug("{} records are processed so far, rest {}", processed, dir.getChildren().size());
	    } catch (InterruptedException | ExecutionException e) {
		_logger.error(e.getMessage());
	    }

	}

	final StringBuffer sb = new StringBuffer("\n");

	sb.append("\t").append("Total number of records: " + dir.getNumOfFiles() + "\n");
	sb.append("\t").append("Total number of valid records: " + dir.getNumOfValidFiles() + "\n");
	sb.append("\t").append("Average number of valid records: "
		+ divAndFormat(dir.getNumOfValidFiles(), dir.getNumOfFiles(), "0%") + "\n");
	sb.append("\t").append("Total file size: " + dir.getSize() + " bytes" + "\n");
	sb.append("\t").append(
		"Average file size: " + divAndFormat(dir.getSize(), dir.getNumOfFiles(), "0.00") + " bytes" + "\n");
	sb.append("\t")
		.append("max/min file size: " + dir.getMaxFileSize() + "/" + dir.getMinFileSize() + " bytes" + "\n");

	if (Config.PRINT_COLLECTION_DETAILS)
	    sb.append("\n------------------------------------------------\nDETAILS").append(childReport);

	report.addMessage(sb.toString());
	
	return report;
    }

    private String divAndFormat(double a, double b, String format) {
	return new DecimalFormat(format).format(a / b);
    }

}
