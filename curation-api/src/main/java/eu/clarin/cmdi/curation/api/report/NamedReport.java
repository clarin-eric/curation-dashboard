/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report;

import java.time.LocalDateTime;

/**
 *
 */
public interface NamedReport {
   
   public String getName();

   public LocalDateTime getCreationTime();

   public void setPreviousCreationTime(LocalDateTime previousCreationTime);

   public LocalDateTime getPreviousCreationTime();

}
