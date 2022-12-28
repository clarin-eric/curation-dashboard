package eu.clarin.cmdi.curation.api.subprocessor.profile;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.Score.Severity;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.cr.CRService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProfileHeaderHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport>{

   @Autowired
   private CRService crService;


   public void process(CMDProfile entity, CMDProfileReport report) { 
         
      report.headerReport.setHeader(crService.createProfileHeader(entity.getSchemaLocation(), "1.x", false));

      if (!report.headerReport.getHeader().isPublic()) {
         report.headerReport.getScore().addMessage(Severity.WARNING, "Profile is not public");
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


   }
}
