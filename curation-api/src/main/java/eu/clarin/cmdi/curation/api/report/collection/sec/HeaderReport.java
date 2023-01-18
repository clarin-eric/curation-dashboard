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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;



/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HeaderReport {
   
   public int totNumOfProfiles;
   
   @XmlElementWrapper(name = "duplicatedMDSelfLinks")
   private Collection<String> duplicatedMDSelfLink;
   
   public Collection<Profile> profiles = new ArrayList<Profile>();  


   @XmlRootElement(name = "profile")
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class Profile {
      @XmlAttribute
      public final String name;
      @XmlAttribute
      public final double score;
      @XmlAttribute
      public int count;
   }
}
