package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;


/**
 * @author dostojic
 *  
 */

@FunctionalInterface
public interface CurationStep<T extends CurationEntity>{
	
	public boolean apply(T entity);

}
