/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.sec;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;
import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport.Concept;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@XmlRootElement(name = "facets")
@XmlAccessorType(XmlAccessType.NONE)
public class InstanceFacetReport extends ScoreReport {

   private Collection<Coverage> coverages = new ArrayList<Coverage>();
   private Collection<ValueNode> valueNodes = new ArrayList<ValueNode>();

   @XmlAttribute
   public int getNumOfFacets() {
      return this.coverages.size();
   }

   @XmlAttribute
   public double getInstanceCoverage() {
      return this.coverages.size()!=0?this.coverages.stream().filter(Coverage::isCoveredByInstance).count()/this.coverages.size():0.0;
   };

   @XmlElementWrapper
   @XmlElement(name = "facet")
   public Collection<Coverage> getCoverages() {
      return this.coverages;
   }

   @XmlElementWrapper
   @XmlElement(name = "valueNode")
   public Collection<ValueNode> getValueNodes() {
      return this.valueNodes;
   };
   
   public void addCoverage(String facetName, boolean coveredByProfile) {
      this.coverages.add(new Coverage(facetName, coveredByProfile));
   }
   public ValueNode getValueNode(String value, String xpath) {
      ValueNode node = new ValueNode(value, xpath);
      this.valueNodes.add(node);
      return node;
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @Getter
   public class ValueNode {
      
      public ValueNode(String value, String xpath) {
         this.xpath = xpath;
         this.value = value;
      };

      @XmlElement
      private String value;

      @XmlElement
      private String xpath;

      @XmlElement
      private Concept concept;

      @XmlElement
      private Collection<FacetValueStruct> facet = new ArrayList<FacetValueStruct>();
      
      public void setConcept(Concept concept) {
         this.concept = concept;
      }
      public void addFacet(String facetName, boolean isDerived, boolean usesValueMapping, String normalisedValue) {
         
      }
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public class FacetValueStruct {

      @XmlAttribute
      private final String name;

      @XmlAttribute
      private final boolean isDerived;

      @XmlAttribute
      private final boolean usesValueMapping;

      @XmlAttribute
      private final String normalisedValue;

   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @Data
   public class Coverage {     
      
      public Coverage(String name, boolean coveredByProfile) {
         this.name = name;
         this.coveredByProfile = coveredByProfile;
      }
      @XmlAttribute
      private String name;

      @XmlAttribute
      private boolean coveredByProfile;

      @XmlAttribute
      private boolean coveredByInstance;

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
            return InstanceFacetReport.this.getInstanceCoverage();
         }
      };
   }
}