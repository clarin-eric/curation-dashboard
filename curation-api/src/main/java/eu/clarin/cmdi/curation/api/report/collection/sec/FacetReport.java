/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FacetReport {
   @XmlAttribute
   public static final double maxScore = eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.maxScore;
   @XmlAttribute
   public double aggregatedScore = 0.0;
   @XmlAttribute
   public double avgScore;
   @XmlAttribute
   public double avgScoreValid;  
   @XmlElement
   public double percCoverageNonZero;
   @XmlElementWrapper(name = "facets")
   @XmlElement(name = "facet")
   public Collection<FacetCollectionStruct> facets = new ArrayList<FacetCollectionStruct>();

   
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class FacetCollectionStruct { 
      @XmlAttribute
      public final String name;
      @XmlAttribute
      public int count; // num of records covering it
      @XmlAttribute
      public double coverage;

   }
}
