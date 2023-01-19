/**
 * 
 */
package eu.clarin.cmdi.curation.api.subprocessor;

public abstract class AbstractSubprocessor<E, R> {

    public abstract void process(E entity, R report);

}
