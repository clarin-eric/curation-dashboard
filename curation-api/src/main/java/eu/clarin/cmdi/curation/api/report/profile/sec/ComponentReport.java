/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import lombok.NoArgsConstructor;
import eu.clarin.cmdi.curation.api.report.Scoring;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ComponentReport extends ScoreReport {
   

   private int total;

   private int required; // cardinality > 0

   private Map<String, Component> componentMap = new HashMap<String, Component>();

   @XmlAttribute
   public int getTotal() {
      return this.total;
   }
   @XmlAttribute
   public int getUnique() {
      return componentMap.size();
   }
   @XmlAttribute
   public int getRequired() {
      return this.required;
   }
   @XmlElement(name = "component")
   public Collection<Component> getComponents(){
      return this.componentMap.values();
   }
   public Component getComponent(CMDINode node) {
      return this.componentMap.computeIfAbsent(node.component.id, k -> new Component(node));
   }
   public void incrementTotal() {
      this.total++;
   }
   public void incrementRequired() {
      this.required++;
   }
   
   @XmlRootElement()
   @XmlAccessorType(XmlAccessType.NONE)
   @NoArgsConstructor(force = true)
   public class Component {
      
      public Component(CMDINode node) {
         this.id = node.component.id;
         this.name = node.component.name;
      }

       @XmlAttribute
       private final String id;

       @XmlAttribute
       private final String name;

       @XmlAttribute
       private int count;
       
       public void incrementCount() {
          this.count++;
       }      
       
   }

   @Override
   public Scoring newScore() {
      return new Scoring() {
         @Override
         public double getMaxScore() {
            return 1.0;
         }
         @Override
         public double getScore() {
            // TODO Auto-generated method stub
            return 0;
         }         
      };
   }
}
