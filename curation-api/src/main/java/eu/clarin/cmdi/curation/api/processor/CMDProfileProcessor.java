/**
 *
 */
package eu.clarin.cmdi.curation.api.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.cache.ProfileReportCache;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;


/**
 *
 */
@Component
@Scope("prototype")
public class CMDProfileProcessor {
   
   @Autowired
   ProfileReportCache profileReportCache;

   public CMDProfileReport process(CMDProfile profile) {

      //we delegate the processing to a cache
      return profileReportCache.getProfileReport(profile);

   }
}
