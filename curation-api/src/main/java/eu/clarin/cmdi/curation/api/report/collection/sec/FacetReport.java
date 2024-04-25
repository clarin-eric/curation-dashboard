/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import jakarta.xml.bind.annotation.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FacetReport {
   @XmlAttribute
   public double aggregatedScore;
   @XmlAttribute
   public double aggregatedMaxScore;
   @XmlAttribute
   public double scorePercentage;
   @XmlAttribute
   public double avgScore;
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
      public double avgCoverage;

   }
}
