/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
public class XmlValidityReport {
   
   private long totNumOfRecords;
   private int totNumOfValidRecords;
   private Double ratioOfValidRecords = 0.0;
   
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Record {
      @XmlAttribute
      public String name;

      @XmlElement(name = "issue")
      public Collection<String> issues;
   }

}
