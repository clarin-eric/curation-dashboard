/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;



/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HeaderReport {
   
   @XmlElementWrapper(name = "duplicatedMDSelfLinks")
   private Collection<String> duplicatedMDSelfLink;
   
   private Map<String, Profile> profileMap = new HashMap<String, Profile>();
   
   public int getTotNumOfProfiles() {
      return this.profileMap.size();
   }
   
   public Collection<Profile> getProfiles(){
      return this.profileMap.values();
   }


   @XmlRootElement(name = "profile")
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Profile {

      @XmlAttribute
      private String name;

      @XmlAttribute
      private int count;

      @XmlAttribute
      private double score;
   }

}
