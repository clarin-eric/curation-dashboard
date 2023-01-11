/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.sec;

import eu.clarin.cmdi.curation.api.report.ScoreReport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.Scoring;
import lombok.Getter;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
public class XmlPopulationReport extends ScoreReport {
   
   private int numOfXMLElements;
   private int numOfXMLSimpleElements;
   private int numOfXMLEmptyElements;
   
   public void incrementNumOfXMLElements() {
      numOfXMLElements++;
   }
   public void incrementNumOfXMLSimpleElements() {
      numOfXMLSimpleElements++;
   }
   public void incrementNumOfXMLEmptyElements() {
      numOfXMLEmptyElements++;
   }
   
   public double getPercOfPopulatedElements() {
      return numOfXMLSimpleElements>0?(numOfXMLSimpleElements - numOfXMLEmptyElements)/numOfXMLSimpleElements:0.0;
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
            // TODO Auto-generated method stub
            return 0;
         }  
      };
   }
}
