/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import jakarta.xml.bind.annotation.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ComponentReport implements Serializable {
   @XmlAttribute
   public int total;
   @XmlAttribute
   public int unique;
   @XmlAttribute
   public int required;
   @XmlElement(name = "component")
   public Collection<Component> components;

   @XmlRootElement()
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class Component implements Serializable {

      @XmlAttribute
      public final String id;
      @XmlAttribute
      public final String name;
      @XmlAttribute
      public int count;

   }
}
