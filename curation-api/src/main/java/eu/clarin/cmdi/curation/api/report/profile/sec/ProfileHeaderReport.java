/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import eu.clarin.cmdi.curation.pph.ProfileHeader;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"id", "schemaLocation", "name", "description", "cmdiVersion", "status", "public"})
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class ProfileHeaderReport {
   @XmlAttribute
   public static final double maxScore = 1.0;
   @XmlAttribute
   public double score;
   private final ProfileHeader header;  

   @XmlTransient
   public ProfileHeader getProfileHeader() {
      return header;
   }
   @XmlElement
   public String getId() {
      return header.getId();
   }
   @XmlElement
   public String getSchemaLocation() {
      return header.getSchemaLocation();
   }
   @XmlElement
   public String getName() {
      return header.getName();
   }
   @XmlElement
   public String getDescription() {
      return header.getDescription();
   }
   @XmlElement
   public String getCmdiVersion() {
      return header.getCmdiVersion();
   }
   @XmlElement
   public String getStatus() {
      return header.getStatus();
   }
   @XmlElement
   public boolean isPublic() {
      return header.isPublic();
   }
}
