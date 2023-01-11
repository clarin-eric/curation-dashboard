/**
 * 
 */
package eu.clarin.cmdi.curation.api.report;


/**

 *
 */

public abstract class ScoreReport {
   
   private Scoring scoring;
   
   public ScoreReport() {     
      this.scoring = newScore();     
   }
   
   public Scoring getScoring() {
      return this.scoring;
   }
	
	public boolean isValid() {	   
	   return !this.scoring.hasFatalMessage();	
	};
	
	public abstract Scoring newScore();
	
}
