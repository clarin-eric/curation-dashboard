/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.sec;

import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport.Concept;
import jakarta.xml.bind.annotation.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
@XmlRootElement(name = "facets")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstanceFacetReport {
   @XmlAttribute
   public static final double maxScore = 1.0;
   @XmlAttribute
   public double score;
   @XmlAttribute
   public int numOfFacets;
   @XmlAttribute
   public int numOfFacetsCoveredByInstance;   
   @XmlAttribute
   public double percCoveragedByInstance;

   @XmlElementWrapper
   @XmlElement(name = "facet")
   public Collection<Coverage> coverages = new ArrayList<Coverage>();
   @XmlElementWrapper
   @XmlElement(name = "valueNode")
   public Collection<ValueNode> valueNodes = new ArrayList<ValueNode>();

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class ValueNode {
      @XmlElement
      public final String xpath;
      @XmlElement
      public final String value;
      @XmlElement
      public Concept concept;

      @XmlElement(name = "facet")
      public Collection<FacetValueStruct> facets = new ArrayList<FacetValueStruct>();
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class FacetValueStruct {
      @XmlAttribute
      public final String name;
      @XmlAttribute
      public final boolean isDerived;
      @XmlAttribute
      public final boolean usesValueMapping;
      @XmlAttribute
      public final String normalisedValue;
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class Coverage {     
      @XmlAttribute
      public final String name;
      @XmlAttribute
      public final boolean coveredByProfile;
      @XmlAttribute
      public boolean coveredByInstance;
   }
}