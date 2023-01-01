/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.section;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
@Setter
public class FileReport extends ScoreReport{
   
   private String location;
   private long size;
   private String collection;

   public FileReport() {
   }

   @Override
   public Scoring newScore() {     
      return new Scoring() {

         @Override
         public double getMaxScore() {           
            return 1.0;
         }
         @Override
         public double getScore() {           
            return this.hasFatalMsg()?0.0:1.0;        
         }         
      };      
   }
}
