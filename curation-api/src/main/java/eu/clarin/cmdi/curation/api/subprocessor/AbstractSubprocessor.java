/**
 * 
 */
package eu.clarin.cmdi.curation.api.subprocessor;

import eu.clarin.cmdi.curation.api.report.ScoreReport;

public abstract class AbstractSubprocessor<E, R extends ScoreReport> {

    public abstract void process(E entity, R report);

}
