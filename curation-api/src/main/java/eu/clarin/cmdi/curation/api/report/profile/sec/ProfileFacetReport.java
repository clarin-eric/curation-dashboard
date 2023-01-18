/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


/**
 *
 */
@XmlRootElement(name = "facets")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileFacetReport extends ScoreReport {
   @XmlAttribute
   public int numOfFacets;  
   @XmlAttribute
   public int profileCoverage;
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