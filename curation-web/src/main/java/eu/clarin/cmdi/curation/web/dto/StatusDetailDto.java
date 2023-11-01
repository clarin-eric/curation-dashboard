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
 * The type Status detail dto.
 */
@ToString
@JsonRootName(value = "link")
@JsonPropertyOrder({"url", "checkingDate", "method", "statusCode", "contentLength", "duration", "redirects", "message", "collection", "origin", "expectedMimeType"})
public class StatusDetailDto {
   
   private StatusDetail statusDetail;

   /**
    * Instantiates a new Status detail dto.
    *
    * @param statusDetail the status detail
    */
   public StatusDetailDto(StatusDetail statusDetail) {
      
      this.statusDetail = statusDetail;
   }

   /**
    * Gets url.
    *
    * @return the url
    */
   public String getUrl() {
      
      return this.statusDetail.getUrlname();
   }

   /**
    * Gets checking date.
    *
    * @return the checking date
    */
   @JsonSerialize(using = LocalDateTimeSerializer.class)
   @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
   public LocalDateTime getCheckingDate() {
      
      return this.statusDetail.getCheckingDate();
   }

   /**
    * Gets method.
    *
    * @return the method
    */
   public String getMethod() {
      
      return this.statusDetail.getMethod();
   }

   /**
    * Gets status code.
    *
    * @return the status code
    */
   public Integer getStatusCode() {
      
      return this.statusDetail.getStatusCode();
   }

   /**
    * Gets content length.
    *
    * @return the content length
    */
   public Long getContentLength() {
      
      return this.statusDetail.getContentLength();
   }

   /**
    * Gets duration.
    *
    * @return the duration
    */
   public Integer getDuration() {
      
      return this.statusDetail.getDuration();
   }

   /**
    * Gets redirects.
    *
    * @return the redirects
    */
   public Integer getRedirects() {
      
      return this.statusDetail.getRedirectCount();
   }

   /**
    * Gets message.
    *
    * @return the message
    */
   public String getMessage() {
      
      return this.statusDetail.getMessage();
   }

   /**
    * Gets collection.
    *
    * @return the collection
    */
   public String getCollection() {
      
      return this.statusDetail.getProvidergroupname();
   }

   /**
    * Gets origin.
    *
    * @return the origin
    */
   public String getOrigin() {
      
      return this.statusDetail.getOrigin();
   }

   /**
    * Gets expected mime type.
    *
    * @return the expected mime type
    */
   public String getExpectedMimeType() {
      
      return this.statusDetail.getExpectedMimeType();
   }
   
   //"url\tcheckingDate\tmethod\tstatusCode\tbyteSize\tduration\tredirects\tmessage\tcollection\torigin\texpected-mime-type\n"

}
