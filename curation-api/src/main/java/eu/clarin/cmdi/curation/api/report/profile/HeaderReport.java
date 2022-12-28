/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.api.report.Score;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class HeaderReport extends ScoreReport {
   
   private ProfileHeader header;  

   @XmlTransient
   public ProfileHeader getHeader() {
      return header;
   }


   public void setHeader(ProfileHeader header) {
      this.header = header;
   }
   
   public String getId() {
      return header.getId();
   }


   public String getSchemaLocation() {
      return header.getSchemaLocation();
   }


   public String getName() {
      return header.getName();
   }


   public String getDescription() {
      return header.getDescription();
   }


   public String getCmdiVersion() {
      return header.getCmdiVersion();
   }


   public String getStatus() {
      return header.getStatus();
   }


   @Override
   public Score newScore() {
      // TODO Auto-generated method stub
      return new Score() {
         @Override
         public double getMax() {
            return 1;
         }
         @Override
         public double getCurrent() {
            // TODO Auto-generated method stub
            return 0;
         }
      };
   }
}
