/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import java.util.Collection;
import java.util.TreeSet;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import eu.clarin.linkchecker.persistence.utils.Category;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LinkcheckerReport {
   @XmlAttribute
   public double aggregatedScore;
   @XmlAttribute
   public double aggregatedMaxScore;
   @XmlAttribute
   public double scorePercentage;
   @XmlAttribute
   public double avgScore;
   @XmlElement
   public int totNumOfLinks;
   @XmlElement
   public double avgNumOfLinks;
   @XmlElement
   public int totNumOfUniqueLinks;
   @XmlElement
   public double avgNumOfUniqueLinks;
   @XmlElement
   public int totNumOfCheckedLinks;
   @XmlElement
   public int totNumOfLinksWithDuration;
   @XmlElement
   public double ratioOfValidLinks;
   @XmlElement
   public Double avgRespTime;
   @XmlElement
   public Long maxRespTime;
   @XmlElementWrapper(name = "linkchecker")
   public Collection<Statistics> statistics = new TreeSet<Statistics>();
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
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
      public final Category category;


      @Override
      public int compareTo(Statistics other) {

         return this.category.compareTo(other.category);
      }
   }
}
