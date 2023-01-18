/**
 * 
 */
package eu.clarin.cmdi.curation.api.report;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**

 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ScoreReport {
   @XmlElement
   public final Scoring scoring;
   
   public ScoreReport() {     
      this.scoring = new Scoring();     
   }

	
	public boolean isValid() {	   
	   return !this.scoring.hasFatalMessage();	
	};	
}
