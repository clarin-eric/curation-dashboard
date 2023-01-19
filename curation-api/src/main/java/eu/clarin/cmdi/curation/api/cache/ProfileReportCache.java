/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.processor.CMDProfileProcessor;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;


/**
 *
 */
@Component
public class ProfileReportCache {
   @Autowired
   CMDProfileProcessor processor;
   
   @Cacheable(value = "publicProfileReportCache", key = "#profile.schemaLocation")
   public CMDProfileReport getProfileReport(CMDProfile profile) {
      
      return processor.process(profile);
      
   }
}
