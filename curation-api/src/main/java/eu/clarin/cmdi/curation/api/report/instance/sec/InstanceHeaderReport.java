/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.sec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;

/**
 *
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InstanceHeaderReport extends ScoreReport {
   
   public String schemaLocation;
   
   public String mdProfile;
   
   public String mdCollectionDisplayName;
   
   public String mdSelfLink;

}
