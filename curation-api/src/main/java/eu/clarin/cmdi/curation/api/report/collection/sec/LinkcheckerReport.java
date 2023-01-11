/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import java.util.Collection;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.clarin.linkchecker.persistence.utils.Category;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
@Setter
public class LinkcheckerReport {
   
   private int totNumOfLinks;
   private Double avgNumOfLinks = 0.0;
   private int totNumOfUniqueLinks;
   private int totNumOfCheckedLinks;
   private Double avgNumOfUniqueLinks = 0.0;
   private int totNumOfInvalidLinks;
   private int totNumOfBrokenLinks;
   private Double avgNumOfBrokenLinks = 0.0;
   private Double ratioOfValidLinks = 0.0;
   private int totNumOfUndeterminedLinks;
   private int totNumOfRestrictedAccessLinks;
   private int totNumOfBlockedByRobotsTxtLinks;
   private Double avgRespTime = 0.0;
   private Long maxRespTime = 0L;
   @XmlElementWrapper(name = "linkchecker")
   private Collection<Statistics> statistics = new TreeSet<Statistics>();
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Statistics implements Comparable<Statistics> {      
      
      @XmlAttribute
      public long count;

      @XmlTransient
      public long nonNullCount;

      @XmlAttribute
      public double avgRespTime;

      @XmlAttribute
      public double maxRespTime;

      @XmlAttribute
      public Category category;

      public Statistics() {

      }

      public Statistics(Category category) {
         this.category = category;
      }

      @Override
      public int compareTo(Statistics other) {

         return this.category.compareTo(other.category);
      }
   }

}
