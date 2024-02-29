/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 */
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

   private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

   @Override
   public String marshal(LocalDateTime dateTime) {
      
       return dateTime.format(dateFormat);
   
   }

   @Override
   public LocalDateTime unmarshal(String dateTime) {
      
       return LocalDateTime.parse(dateTime, dateFormat);
   
   }
}
