/**
 * 
 */
package eu.clarin.cmdi.curation.api.subprocessor;

import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;

/**
 * The type Abstract subprocessor.
 *
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
public abstract class AbstractSubprocessor<E, R> {

    /**
     * Process.
     *
     * @param entity the entity
     * @param report the report
     */
    public abstract void process(E entity, R report) throws MalFunctioningProcessorException;

}
