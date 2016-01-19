package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Report;

/**
 * @author dostojic
 *
 */
public class Dummy extends CurationTask {

	@Override
	public Report generateReport(CurationEntity entity) {
		return new Report("");
	}

}
