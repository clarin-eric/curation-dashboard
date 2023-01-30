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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.clarin.linkchecker.persistence.utils.Category;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LinkcheckerReport {
   @XmlAttribute
   public static final double maxScore = 1.0;
   @XmlAttribute
   public double aggregatedScore = 0.0;
   @XmlAttribute
   public double avgScore;
   @XmlAttribute
   public double avgScoreValid;  
   @XmlAttribute
   public double score;
   @XmlElement
   public int totNumOfLinks;
   @XmlElement
   public int totNumOfUniqueLinks;
   @XmlElement
   public double avgNumOfLinks;
   @XmlElement
   public int totNumOfCheckedLinks;
   @XmlElement
   public double avgNumOfUniqueLinks;
   @XmlElement
   public int totNumOfInvalidLinks;
   @XmlElement
   public int totNumOfBrokenLinks;
   @XmlElement
   public double avgNumOfBrokenLinks;
   @XmlElement
   public double ratioOfValidLinks;
   @XmlElement
   public int totNumOfUndeterminedLinks;
   @XmlElement
   public int totNumOfRestrictedAccessLinks;
   @XmlElement
   public int totNumOfBlockedByRobotsTxtLinks;
   @XmlElement
   public Double avgRespTime;
   @XmlElement
   public Long maxRespTime;
   @XmlElementWrapper(name = "linkchecker")
   public Collection<Statistics> statistics = new TreeSet<Statistics>();
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Statistics implements Comparable<Statistics> {      
      
      @XmlAttribute
      public long count;

      @XmlTransient
      public long nonNullCount;

      @XmlAttribute
      public Double avgRespTime;

      @XmlAttribute
      public Long maxRespTime;

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
