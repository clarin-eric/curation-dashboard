/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import jakarta.xml.bind.annotation.*;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FileReport {
   @XmlAttribute
   public double aggregatedScore;
   @XmlAttribute
   public double aggregatedMaxScore;
   @XmlAttribute
   public double scorePercentage;
   @XmlAttribute
   public double avgScore;
   @XmlElement
   public String collectionRoot;
   @XmlElement
   public String provider;
   @XmlElement
   public long numOfFiles;
   @XmlElement
   public long numOfFilesProcessable;
   @XmlElement
   public long numOfFilesNonProcessable;
   @XmlElement
   public long size;
   @XmlElement
   public long minFileSize;
   @XmlElement
   public long maxFileSize;
   @XmlElement
   public long avgFileSize;
   
}
