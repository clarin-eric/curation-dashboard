/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 */
@Slf4j
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

   private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

   @Override
   public String marshal(LocalDateTime dateTime) {
      
       return dateTime!=null?dateTime.format(dateFormat):null;
   
   }

   @Override
   public LocalDateTime unmarshal(String dateTime) {

       if(StringUtils.isNotBlank(dateTime)){
           try{

               return LocalDateTime.parse(dateTime, dateFormat);
           }
           catch(DateTimeException ex){

               log.error("the string '{}' doesn't fit the pattern 'yyyy-MM-dd HH:mm:ss'", dateTime);
           }
       }
      return null;
   }
}
