/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.ccr.CCRConcept;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import eu.clarin.cmdi.curation.api.report.Scoring;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
public class ConceptReport extends ScoreReport {
   
   @XmlAttribute
   private int total;

   @XmlAttribute
   private int required; // cardinality > 0

   @XmlAttribute
   private int withConcept;
   
   @XmlAttribute
   public int getUnique() {
      return this.concepts.size();
   };
   
   public void incrementTotal() {
      this.total++;
   }
   public void incrementRequired() {
      this.required++;
   }
   public void incrementWithConcept() {
      this.withConcept++;
   }

   @XmlAttribute
   public double getPercWithConcept() {
      return (total>0?withConcept/total:0.0);
   };
   
   @XmlElement(name = "concept")
   public java.util.Collection<Concept> concepts = new ArrayList<Concept>();


   
   

   @Override
   public Scoring newScore() {
      return new Scoring() {
         @Override
         public double getMaxScore() {
            return 1;
         }
         @Override
         public double getScore() {
            return ConceptReport.this.getPercWithConcept();
         }
      };
   }
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class Concept {

      private final CCRConcept ccrConcept;
      private int count = 1;

      @XmlAttribute
      public String getUri() {
         return this.ccrConcept.getUri();
      }

      @XmlAttribute
      public String getPrefLabel() {
         return this.ccrConcept.getPrefLabel();
      };

      @XmlAttribute
      public String getStatus() {
         return this.getStatus();
      };

      @XmlAttribute
      public int getCount() {
         return this.count;
      }
      
      public void incrementCount() {
         this.count++;
      }

   }

}
