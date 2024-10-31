/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import eu.clarin.cmdi.curation.ccr.CCRConcept;
import jakarta.xml.bind.annotation.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

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
