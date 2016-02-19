package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Report;

/**
 * @author dostojic
 * 
 */


public interface ProcessingStep<T extends CurationEntity, R extends Report> {


    /**
     * @param entity 
     * @param report
     * @return true if processing finished without fatal errors
     */
    public boolean process(T entity, R report);
}