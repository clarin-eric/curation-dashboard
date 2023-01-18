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


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LinkcheckerReport {
   
   public int totNumOfLinks;
   public double avgNumOfLinks;
   public int totNumOfUniqueLinks;
   public int totNumOfCheckedLinks;
   public double avgNumOfUniqueLinks;
   public int totNumOfInvalidLinks;
   public int totNumOfBrokenLinks;
   public double avgNumOfBrokenLinks;
   public double ratioOfValidLinks;
   public int totNumOfUndeterminedLinks;
   public int totNumOfRestrictedAccessLinks;
   public int totNumOfBlockedByRobotsTxtLinks;
   public double avgRespTime;
   public long maxRespTime;
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
