package eu.clarin.cmdi.curation.api.subprocessor.profile;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileHeaderReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoCRCacheEntryException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type Profile header handler.
 */
@Slf4j
@Component
public class ProfileHeaderHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport> {

   @Autowired
   private ApiConfig conf;
   @Autowired
   private CRService crService;


   /**
    * Process.
    *
    * @param profile the profile
    * @param report  the report
    */
   public void process(CMDProfile profile, CMDProfileReport report) throws MalFunctioningProcessorException {

      try {
         report.headerReport = new ProfileHeaderReport(
                 crService.createProfileHeader(profile.getSchemaLocation()));


         if (!report.headerReport.getProfileHeader().isPublic()) {
            log.debug("profile {} not public", profile.getSchemaLocation());
            report.details.add((new Detail(Severity.WARNING,"header" , "Profile is not public")));
         }
         else {
            report.headerReport.score = 1;
            report.score += report.headerReport.score;
         }
/* we need a better solution here since these warnings depend on the order
         pphService.getProfileHeaders().stream()
               .filter(profileHeader -> profileHeader.getName().equals(report.headerReport.getName())
                     && !profileHeader.getId().equals(report.headerReport.getId()))
               .findAny().ifPresent(profileReport -> report.details.add(new Detail(Severity.WARNING,"header",
                     "The name '" + report.headerReport.getName() + "' of the profile is not unique")));

         pphService.getProfileHeaders().stream()
               .filter(profileHeader -> profileHeader.getDescription().equals(report.headerReport.getDescription())
                     && !profileHeader.getId().equals(report.headerReport.getId()))
               .findAny().ifPresent(profileReport -> report.details.add(new Detail(Severity.WARNING,"header",
                     "The description '" + report.headerReport.getDescription() + "' of the profile is not unique")));
      */
      }
      catch (NoCRCacheEntryException e) {
         report.details.add(new Detail(Severity.FATAL,"concept" , "can't get ParsedProfile for profile id '" + report.headerReport.getId() + "'"));
         log.debug("can't get ParsedProfile for profile id '{}'", report.headerReport.getId());
         return;

      }
      catch (CRServiceStorageException | CCRServiceNotAvailableException | PPHCacheException e) {
          throw new MalFunctioningProcessorException(e);
      }
   }
}
