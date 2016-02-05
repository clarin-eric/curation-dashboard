package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 * 
 */

public interface ProcessingActivity<T extends CurationEntity> {

    public Severity process(T entity);
}