/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.web.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import eu.clarin.linkchecker.persistence.model.StatusDetail;
import lombok.ToString;


/**
 *
 */
@ToString
@JsonRootName(value = "link")
@JsonPropertyOrder({"url", "checkingDate", "method", "statusCode", "contentLength", "duration", "redirects", "message", "collection", "origin", "expectedMimeType"})
public class StatusDetailDto {
   
   private StatusDetail statusDetail;
   
   public StatusDetailDto(StatusDetail statusDetail) {
      
      this.statusDetail = statusDetail;
   }
   
   public String getUrl() {
      
      return this.statusDetail.getUrlname();
   }
   
   @JsonSerialize(using = LocalDateTimeSerializer.class)
   @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
   public LocalDateTime getCheckingDate() {
      
      return this.statusDetail.getCheckingDate();
   }
   
   public String getMethod() {
      
      return this.statusDetail.getMethod();
   }
   
   public Integer getStatusCode() {
      
      return this.statusDetail.getStatusCode();
   }
   
   public Long getContentLength() {
      
      return this.statusDetail.getContentLength();
   }
   
   public Integer getDuration() {
      
      return this.statusDetail.getDuration();
   }
   
   public Integer getRedirects() {
      
      return this.statusDetail.getRedirectCount();
   }
   
   public String getMessage() {
      
      return this.statusDetail.getMessage();
   }
   
   public String getCollection() {
      
      return this.statusDetail.getProvidergroupname();
   }
   
   public String getOrigin() {
      
      return this.statusDetail.getOrigin();
   }
   
   public String getExpectedMimeType() {
      
      return this.statusDetail.getExpectedMimeType();
   }
   
   //"url\tcheckingDate\tmethod\tstatusCode\tbyteSize\tduration\tredirects\tmessage\tcollection\torigin\texpected-mime-type\n"

}
