/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;
import lombok.Getter;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class FileReport extends ScoreReport {
   
   private String provider;
   private long numOfFiles;
   private long size;
   private long minFileSize;
   private long maxFileSize;
   
   public long getAvgSize() {
      return this.numOfFiles!=0?this.size/this.numOfFiles:0;
   }
   
   public void incrementNumOfFiles() {
      this.numOfFiles++;
   }
   public void addSize(long addition) {
      this.size+=addition;
   }
   public void setProvider(String provider) {
      this.provider = provider;
   }
   public void setMinFileSize(long minFileSize) {
      this.minFileSize = minFileSize;
   }
   public void setMaxFileSize(long maxFileSize) {
      this.maxFileSize = maxFileSize;
   }

   @Override
   public Scoring newScore() {
      // TODO Auto-generated method stub
      return null;
   }

}
