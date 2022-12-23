/**
 * 
 */
package eu.clarin.cmdi.curation.api.subprocessor;

import eu.clarin.cmdi.curation.api.report.Report;

public abstract class AbstractSubprocessor<E, R extends Report<?>>{

    public abstract void process(E entity, R report);

}
