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
import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport.Concept;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@XmlRootElement(name = "facets")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstanceFacetReport extends ScoreReport {
   @XmlAttribute
   public int numOfFacets;
   @XmlAttribute
   public double instanceCoverage;

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
      public final String value;
      @XmlElement
      public final String xpath;
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