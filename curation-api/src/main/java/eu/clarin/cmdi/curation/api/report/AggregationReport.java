/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report;

/**
 * The interface Aggregation report.
 *
 * @param <R> the type parameter
 */
public interface AggregationReport<R> {

   /**
    * Add report.
    *
    * @param report the report
    */
   public void addReport(R report);

}
