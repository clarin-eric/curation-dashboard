/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;


import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;
import jakarta.xml.bind.annotation.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"id", "schemaLocation", "name", "description", "cmdiVersion", "status", "public", "crResident"})
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
      return header.id();
   }
   @XmlElement
   public String getSchemaLocation() {
      return header.schemaLocation();
   }
   @XmlElement
   public String getName() {
      return header.name();
   }
   @XmlElement
   public String getDescription() {
      return header.description();
   }
   @XmlElement
   public String getCmdiVersion() {
      return header.cmdiVersion();
   }
   @XmlElement
   public String getStatus() { return header.status(); }
   @XmlElement
   public boolean isPublic() {
      return header.isPublic();
   }
   @XmlElement
   public boolean isCrResident() { return header.isCrResident(); }
}
