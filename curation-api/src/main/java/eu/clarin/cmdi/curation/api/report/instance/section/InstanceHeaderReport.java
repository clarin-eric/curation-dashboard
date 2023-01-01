/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.section;

import eu.clarin.cmdi.curation.api.report.Scoring;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class InstanceHeaderReport extends ScoreReport {
   
   private String schemaLocation;
   
   private String mdProfile;
   
   private String mdCollectionDisplayName;
   
   private String mdSelfLink;

   @Override
   public Scoring newScore() {
      return new Scoring() {
         @Override
         public double getMaxScore() {
            return 5;
         }

         @Override
         public double getScore() {
            return this.hasFatalMsg()?0.0:getMaxScore() - this.getMessages().stream().filter(message -> message.getSeverity() == Severity.ERROR).count();
         }    
      };
   }
}
