package eu.clarin.cmdi.curation.api.subprocessor.profile;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileHeaderReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.pph.PPHService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProfileHeaderHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport> {

   @Autowired
   private ApiConfig conf;
   @Autowired
   private CRService crService;
   @Autowired
   private PPHService pphService;

   public void process(CMDProfile profile, CMDProfileReport report) {

      report.headerReport = new ProfileHeaderReport(
            crService.createProfileHeader(profile.getSchemaLocation(), "1.x", false));
      
      // instance mode is only used by web app for user upload
      // user uploads are not reliable and must therefore be cached separately
      if("instance".equals(this.conf.getMode())){
         report.headerReport.getProfileHeader().setReliable(false);
      }

      if (!report.headerReport.getProfileHeader().isPublic()) {
         log.debug("profile {} not public", profile.getSchemaLocation());
         report.details.add((new Detail(Severity.WARNING,"header" , "Profile is not public")));
      }
      else {
         report.headerReport.score = 1;
         report.score += report.headerReport.score;
      }

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

   }
}
