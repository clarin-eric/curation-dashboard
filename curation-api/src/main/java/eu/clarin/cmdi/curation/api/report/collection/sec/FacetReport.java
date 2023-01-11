/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Getter
public class FacetReport {
   
   private long numOfFiles;
   
   private Map<String, FacetCollectionStruct> facetMap = new HashMap<String, FacetCollectionStruct>();
   
   public void incrementNumOfFiles() {
      this.numOfFiles++;
   }
   
   public void addFacet(String facetName) {
      this.facetMap.put(facetName, new FacetCollectionStruct(facetName));
   }
   
   public double getAvgCoverage() {
      return getFacets().stream().mapToDouble(FacetCollectionStruct::getCoverage).average().getAsDouble();
   }
   
   @XmlElementWrapper(name = "facets")
   public Collection<FacetCollectionStruct> getFacets(){
      return this.facetMap.values();
   }
   
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
   @Getter
   @RequiredArgsConstructor
   public class FacetCollectionStruct {
      @XmlAttribute
      private final String name;

      @XmlAttribute
      private int count; // num of records covering it

      @XmlAttribute
      public double getCoverage() {
         return FacetReport.this.numOfFiles!=0?this.count/FacetReport.this.numOfFiles:0.0;
      };
      
      public void incrementCount() {
         this.count++;
      }
   }
}
