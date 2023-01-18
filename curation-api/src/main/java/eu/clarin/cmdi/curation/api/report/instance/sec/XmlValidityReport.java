/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.sec;

import eu.clarin.cmdi.curation.api.report.ScoreReport;


/**
 *
 */
public class XmlValidityReport extends ScoreReport {
   

   @Override
   public boolean isValid() {
      return scoring.messages.size() < 3;
   }
}
