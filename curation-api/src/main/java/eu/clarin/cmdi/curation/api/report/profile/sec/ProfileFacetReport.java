/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


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