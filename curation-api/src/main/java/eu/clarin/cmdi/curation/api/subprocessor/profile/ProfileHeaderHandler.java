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
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;
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


         if (report.headerReport.getProfileHeader().isPublic()) {

            report.headerReport.score++;
         }
         else {

            log.debug("profile {} not public", profile.getSchemaLocation());
            report.details.add((new Detail(Severity.WARNING,"header" , "Profile is not public")));
         }
         if(report.headerReport.getProfileHeader().isCrResident()) {

            report.headerReport.score++; // CRResidence
         }
         else {

            report.details.add(new Detail(Severity.WARNING, "header", "Schema not registered"));
         }

         report.score += report.headerReport.score;

      }
      catch (NoCRCacheEntryException e) {
         report.details.add(new Detail(Severity.FATAL,"concept" , "can't parse profile '" + profile.getSchemaLocation() + "'"));
         log.debug("can't parse profile '{}'", profile.getSchemaLocation());

         report.headerReport = new ProfileHeaderReport(new ProfileHeader("n/a", profile.getSchemaLocation(), "n/a", "n/a", "n/a", "n/a", false, false));

         return;

      }
      catch (CRServiceStorageException | CCRServiceNotAvailableException | PPHCacheException e) {
          throw new MalFunctioningProcessorException(e);
      }
   }
}
