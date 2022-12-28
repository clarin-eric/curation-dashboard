/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.profile;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.Score.Severity;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.ConceptReport;
import eu.clarin.cmdi.curation.api.report.profile.ComponentReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import lombok.extern.slf4j.Slf4j;

/**

 *
 */
@Slf4j
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProfileElementsHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport> {

   @Autowired
   private CRService crService;

   public void process(CMDProfile profile, CMDProfileReport report) {
      
      ParsedProfile parsedProfile = null;

      try {
         parsedProfile = crService.getParsedProfile(report.headerReport.getHeader());
      }
      catch (NoProfileCacheEntryException e) {

         log.debug("can't get ParsedProfile for profile id '{}'", report.headerReport.getId());
         report.componentReport.getScore().addMessage(Severity.FATAL, "can't get ParsedProfile for profile id '" + report.headerReport.getId() + "'");
         return;

      }

      parsedProfile.getComponents().forEach(crc -> {

         if (crc.isRequired) {
            report.componentReport.required++;
         }
         report.componentReport.components.stream()
               .filter(c -> c.getId().equals(crc.component.id))
               .findFirst()
               .ifPresentOrElse(ComponentReport.Component::incrementCount, () -> report.componentReport.components.add(new ComponentReport.Component(crc)));

      });
      
      parsedProfile
      .getElements()
      .entrySet()
      .stream()
      .filter(entrySet -> entrySet.getKey().startsWith("/cmd:CMD/cmd:Components/"))
      .map(Entry::getValue)
      .forEach(n -> {
         if (n.isRequired)
            report.conceptReport.required++;

         if (n.concept != null) {
            report.conceptReport.withConcept++;

            if (n.isRequired)
               report.conceptReport.required++;

            report.conceptReport.concepts
               .stream()
               .filter(c -> c.getUri().equals(n.concept.getUri())).findFirst()
               .ifPresentOrElse(ConceptReport.Concept::incrementCount, () -> report.conceptReport.concepts.add(new ConceptReport.Concept(n.concept)));
         }  
      });
      

   }

}
