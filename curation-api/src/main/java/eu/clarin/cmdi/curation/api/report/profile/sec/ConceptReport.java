/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import java.util.ArrayList;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.ccr.CCRConcept;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ConceptReport {
   @XmlAttribute
   public static final double maxScore = 1.0;
   @XmlAttribute
   public double score;
   @XmlAttribute
   public int total;
   @XmlAttribute
   public int unique;
   @XmlAttribute
   public int required;
   @XmlAttribute
   public int withConcept;
   @XmlAttribute
   public double percWithConcept;
   
   @XmlElementWrapper(name = "concepts")
   @XmlElement(name = "concept")
   public java.util.Collection<Concept> concepts = new ArrayList<Concept>();
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class Concept {

      private final CCRConcept ccrConcept;
      @XmlAttribute
      public int count;
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
         return this.ccrConcept.getStatus().toString();
      };
   }
}
