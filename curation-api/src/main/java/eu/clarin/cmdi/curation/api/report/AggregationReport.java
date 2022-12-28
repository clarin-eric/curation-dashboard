/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report;

/**
 *
 */
public interface AggregationReport<R extends ScoreReport> {

   public void addReport(R report);

}
