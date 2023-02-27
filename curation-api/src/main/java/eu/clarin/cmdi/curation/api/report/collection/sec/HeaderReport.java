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
public class HeaderReport {
   @XmlAttribute
   public double aggregatedScore;
   @XmlAttribute
   public double aggregatedMaxScoreAll;
   @XmlAttribute
   public double aggregatedMaxScoreProcessable;
   @XmlAttribute
   public double scorePercentageAll;
   @XmlAttribute
   public double scorePercentageProcessable;
   @XmlAttribute
   public double avgScoreAll;
   @XmlAttribute
   public double avgScoreProcessable;
   @XmlElement
   public int totNumOfProfiles;
   
   @XmlElementWrapper(name = "duplicatedMDSelfLinks")
   @XmlElement(name = "duplicatedMDSelfLink")
   public Collection<String> duplicatedMDSelfLink;
   @XmlElementWrapper(name = "profiles")
   @XmlElement(name = "profile")
   public Collection<Profile> profiles = new ArrayList<Profile>();  


   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class Profile {
      @XmlAttribute
      public final String profileId;
      @XmlAttribute
      public final double score;
      @XmlAttribute
      public int count = 1;
   }
}
