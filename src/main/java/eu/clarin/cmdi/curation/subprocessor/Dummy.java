package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Report;

/**
 * @author dostojic
 *
 */
public class Dummy extends CurationStep {

	@Override
	public Report process(CurationEntity entity) {
		return new Report("");
	}

}
