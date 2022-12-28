/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import eu.clarin.cmdi.curation.api.report.Score;


/**
 *
 */
public class ComponentReport extends ScoreReport {
   
   @XmlAttribute
   public int total;

   @XmlAttribute
   public int getUnique() {
      return components.size();
   };

   @XmlAttribute
   public int required; // cardinality > 0

   @XmlElement(name = "component")
   public java.util.Collection<Component> components = new ArrayList<Component>();
   
   @XmlRootElement()
   @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
   @NoArgsConstructor(force = true)
   @Getter
   public static class Component {
      
      public Component(CMDINode node) {
         this.id = node.component.id;
         this.name = node.component.name;
      }

       @XmlAttribute
       private final String id;

       @XmlAttribute
       private final String name;

       @XmlAttribute
       private int count =1;
       
       public void incrementCount() {
          this.count++;
       }      
       
   }

   @Override
   public Score newScore() {
      return new Score() {
         @Override
         public double getMax() {
            return 1.0;
         }
         @Override
         public double getCurrent() {
            // TODO Auto-generated method stub
            return 0;
         }         
      };
   }
}
