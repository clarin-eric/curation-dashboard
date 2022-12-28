/**
 * 
 */
package eu.clarin.cmdi.curation.api.report;


/**

 *
 */

public abstract class ScoreReport {
   
   private Score score;
   
   public ScoreReport() {     
      this.score = newScore();     
   }
   
   public Score getScore() {
      return this.score;
   }
	
	public boolean isValid() {	   
	   return this.score.hasFatalMsg();	
	};
	
	public abstract Score newScore();
	
}
