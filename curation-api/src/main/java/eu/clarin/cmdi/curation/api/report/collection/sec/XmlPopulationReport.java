/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
public class XmlPopulationReport {
   
   private long numOfFiles;
   private int totNumOfXMLElements;
   private int totNumOfXMLSimpleElements;
   private int totNumOfXMLEmptyElements;
   
   public void addTotNumOfXMLElements(int addition) {
      this.numOfFiles++;
      this.totNumOfXMLElements+=addition;
   }
   public void addTotNumOfXMLSimpleElements(int addition) {
      this.totNumOfXMLSimpleElements+=addition;
   }
   public void addTotNumOfXMLEmptyElements(int addition) {
      this.totNumOfXMLEmptyElements+=addition;
   }
   public double getAvgNumOfXMLElements() {
      return this.numOfFiles!=0?this.totNumOfXMLElements/this.numOfFiles:0.0;
   }
   public double getAvgNumOfXMLSimpleElements() {
      return this.numOfFiles!=0?this.totNumOfXMLSimpleElements/this.numOfFiles:0.0;
   }
   public double getAvgXMLEmptyElement() {
      return this.numOfFiles!=0?this.totNumOfXMLEmptyElements/this.numOfFiles:0.0;
   }
   public double getAvgRateOfPopulatedElements() {
      return this.numOfFiles!=0?(this.totNumOfXMLSimpleElements-this.totNumOfXMLEmptyElements)/this.numOfFiles:0.0;
   }
}
