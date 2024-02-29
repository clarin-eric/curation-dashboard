/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.linkchecker;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.linkchecker.persistence.utils.Category;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class LinkcheckerDetailReport implements NamedReport{
   
   @XmlAttribute(name = "provider")
   private final String name; 
   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   private LocalDateTime creationTime = LocalDateTime.now();
   @XmlElement(name = "categoryReport")
   private final Collection<CategoryReport> categoryReports = new TreeSet<CategoryReport>((report1, report2) -> report1.category.compareTo(report2.category));

   public Collection<CategoryReport> getCategoryReports(){
      
      return this.categoryReports;      
   }
   
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlRootElement
   @Data
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class CategoryReport {      
      
      @XmlAttribute
      private final Category category;
      @XmlElement(name = "status")
      private final Collection<StatusDetailReport> statusDetails = new ArrayList<StatusDetailReport>();     
   }
   
   @XmlAccessorType(XmlAccessType.FIELD)
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   @XmlRootElement
   public static class StatusDetailReport {
      
      @XmlAttribute
      private String url;
      @XmlAttribute
      private String method;
      @XmlAttribute
      private Integer statusCode;    
      @XmlAttribute
      private String message;   
      @XmlAttribute
      @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
      private LocalDateTime checkingDate;
      @XmlAttribute
      private String contentType;
      @XmlAttribute
      private Long contentLength;
      @XmlAttribute
      private Integer duration;
      @XmlAttribute
      private Integer redirectCount;
      
      @XmlElementWrapper(name = "contexts")
      @XmlElement(name = "context")
      private final Collection<Context> contexts = new ArrayList<Context>();      
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @Data
   @NoArgsConstructor(force = true)
   @AllArgsConstructor
   @XmlRootElement   
   public static class Context{
      
      @XmlAttribute
      private final String origin;
      @XmlAttribute
      private String expectedMimeType;
   }
}
