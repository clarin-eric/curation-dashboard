package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *  
 */

public abstract class CurationStep<T extends CurationEntity>{
	
	public Severity apply(T entity){
		Report report = process(entity);
		entity.addReport(report);
		return report.getHighestSeverity();
	};
	
	protected abstract Report process(T entity);

}
