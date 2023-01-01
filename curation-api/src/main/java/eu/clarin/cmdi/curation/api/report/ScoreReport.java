/**
 * 
 */
package eu.clarin.cmdi.curation.api.report;


/**

 *
 */

public abstract class ScoreReport {
   
   private Scoring score;
   
   public ScoreReport() {     
      this.score = newScore();     
   }
   
   public Scoring getScore() {
      return this.score;
   }
	
	public boolean isValid() {	   
	   return this.score.hasFatalMsg();	
	};
	
	public abstract Scoring newScore();
	
}
