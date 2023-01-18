/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.subprocessor.profile.ProfileConceptHandler;
import eu.clarin.cmdi.curation.api.subprocessor.profile.ProfileFacetHandler;
import eu.clarin.cmdi.curation.api.subprocessor.profile.ProfileHeaderHandler;


/**
 *
 */
@Component
public class ProfileReportCache {
   @Autowired
   ApplicationContext ctx;
   
   @Cacheable(value = "publicProfileReportCache", key = "#profile.schemaLocation")
   public CMDProfileReport getProfileReport(CMDProfile profile) {
      
      CMDProfileReport report = new CMDProfileReport();

      ProfileHeaderHandler profileHeaderHandler = ctx.getBean(ProfileHeaderHandler.class);
      profileHeaderHandler.process(profile, report);


      ProfileConceptHandler profileElementsHandler = ctx.getBean(ProfileConceptHandler.class);
      profileElementsHandler.process(profile, report);

      ProfileFacetHandler profileFacetHandler = ctx.getBean(ProfileFacetHandler.class);
      profileFacetHandler.process(profile, report);

      return report;
      
   }
}
