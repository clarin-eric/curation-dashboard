/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class Detail {
   public final Severity severity;
   public final String segment;
   public final String message;
   
   public static enum Severity {
      FATAL, ERROR, WARNING, INFO, NONE;
   }
}
