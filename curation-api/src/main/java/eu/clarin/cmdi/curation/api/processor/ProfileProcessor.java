/**
 *
 */
package eu.clarin.cmdi.curation.api.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.subprocessor.profile.ProfileElementsHandler;
import eu.clarin.cmdi.curation.api.subprocessor.profile.ProfileFacetHandler;
import eu.clarin.cmdi.curation.api.subprocessor.profile.ProfileHeaderHandler;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Component
@Scope("prototype")
public class ProfileProcessor {
   
   @Autowired
   ApplicationContext ctx;

   public CMDProfileReport process(CMDProfile profile) {

      CMDProfileReport report = new CMDProfileReport();

      ProfileHeaderHandler profileHeaderHandler = ctx.getBean(ProfileHeaderHandler.class);
      profileHeaderHandler.process(profile, report);


      ProfileElementsHandler profileElementsHandler = ctx.getBean(ProfileElementsHandler.class);
      profileElementsHandler.process(profile, report);

      ProfileFacetHandler profileFacetHandler = ctx.getBean(ProfileFacetHandler.class);
      profileFacetHandler.process(profile, report);

      return report;

   }



}
