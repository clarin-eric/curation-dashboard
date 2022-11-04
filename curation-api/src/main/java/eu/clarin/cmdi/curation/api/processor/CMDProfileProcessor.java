/**
 *
 */
package eu.clarin.cmdi.curation.api.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entities.CMDProfile;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.subprocessor.ext.ProfileElementsHandler;
import eu.clarin.cmdi.curation.api.subprocessor.ext.ProfileFacetHandler;
import eu.clarin.cmdi.curation.api.subprocessor.ext.ProfileHeaderHandler;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Component
@Scope("prototype")
public class CMDProfileProcessor {
   
   @Autowired
   ApplicationContext ctx;
   
   private CMDProfileProcessor() {
      super();
   }

   public CMDProfileReport process(CMDProfile profile) {

      CMDProfileReport report = new CMDProfileReport();

      try {

         ProfileHeaderHandler profileHeaderHandler = ctx.getBean(ProfileHeaderHandler.class);
         profileHeaderHandler.process(profile, report);
         report.addSegmentScore(profileHeaderHandler.calculateScore(report));

         ProfileElementsHandler profileElementsHandler = ctx.getBean(ProfileElementsHandler.class);
         profileElementsHandler.process(report);
         report.addSegmentScore(profileElementsHandler.calculateScore(report));

         ProfileFacetHandler profileFacetHandler = ctx.getBean(ProfileFacetHandler.class);
         profileFacetHandler.process(report);
         report.addSegmentScore(profileFacetHandler.calculateScore(report));
      }
      catch (SubprocessorException ex) {

         log.debug("", ex);
      }

      return report;

   }



}
