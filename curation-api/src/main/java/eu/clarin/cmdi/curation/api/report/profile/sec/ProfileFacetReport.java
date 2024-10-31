/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import jakarta.xml.bind.annotation.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileFacetReport {
   @XmlAttribute
   public static final double maxScore = 1.0;
   @XmlAttribute
   public double score;  
   @XmlAttribute
   public int numOfFacets;
   @XmlAttribute
   public int numOfFacetsCoveredByProfile; 
   @XmlAttribute
   public double percProfileCoverage;
   @XmlElementWrapper(name = "coverage")
   @XmlElement(name = "facet")
   public Collection<Coverage> coverages = new ArrayList<Coverage>();


   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class Coverage {
      @XmlAttribute
      public final String name;
      @XmlAttribute
      public boolean coveredByProfile;

   }
}