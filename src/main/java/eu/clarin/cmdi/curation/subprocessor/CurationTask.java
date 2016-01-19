package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *  
 */

public abstract class CurationTask<T extends CurationEntity>{
	
	public Severity process(T entity){
		Report report = generateReport(entity);
		entity.addReport(report);
		return report.getHighestSeverity();
	};
	
	protected abstract Report generateReport(T entity);

}
