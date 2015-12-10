package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;

/**
 * @author dostojic
 *
 */
public class Dummy implements CurationStep {

	@Override
	public boolean apply(CurationEntity entity) {
		return true;
	}

}
