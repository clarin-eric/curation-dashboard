package eu.clarin.cmdi.curation.api.subprocessor.profile;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.report.Severity;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.pph.PPHService;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.pph.conf.PPHConfig;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProfileHeaderHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport>{

   @Autowired
   private PPHService pphService;
   @Autowired
   private PPHConfig pphConf;


   public void process(CMDProfile entity, CMDProfileReport report) {

      boolean isLocalFile = false;
      Score score = new Score("header-section", 1.0);

      if (entity.getSchemaLocation().startsWith(pphConf.getRestApi())) {
         report.header = pphService.getProfileHeaders().stream()
               .filter(h -> h.getSchemaLocation().equals(entity.getSchemaLocation())).findFirst().orElse(null);
      }

      if (report.header == null) {
         report.header = new ProfileHeader();
         report.header.setSchemaLocation(entity.getSchemaLocation());
         report.header.setCmdiVersion(entity.getCmdiVersion());

         report.header.setPublic(false);
      }

      report.header.setLocalFile(isLocalFile);

      if (!report.header.isPublic()) {
         score.addMessage(Severity.WARNING, "Profile is not public");
      }

      //TODO: verify the intention
      /*
       * if (!crService.isNameUnique(report.header.getName()))
       * addMessage(Severity.WARNING, "The name: " + report.header.getName() +
       * " of the profile is not unique");
       * 
       * if (!crService.isDescriptionUnique(report.header.getDescription()))
       * addMessage(Severity.WARNING, "The description: " +
       * report.header.getDescription() + " of the profile is not unique");
       */
      score.setScore(1.0);
      
      report.addSegmentScore(score);
   }
}
