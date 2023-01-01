/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.section;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;


/**
 *
 */
public class XmlValidityReport extends ScoreReport {
   

   @Override
   public boolean isValid() {
      return getScore().getMessages().size() < 3;
   }

   @Override
   public Scoring newScore() {
      return new Scoring() {
         @Override
         public double getMaxScore() {
            return 1;
         }
         @Override
         public double getScore() {
            return XmlValidityReport.this.isValid()?1.0:0;
         }        
      };
   }
}
