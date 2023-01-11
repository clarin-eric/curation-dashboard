/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.Scoring.Severity;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ComponentReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport;
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
      
      ComponentReport componentReport = new ComponentReport();
      report.setComponentReport(componentReport);
      
      ParsedProfile parsedProfile = null;

      try {
         parsedProfile = crService.getParsedProfile(report.getHeaderReport().getProfileHeader());
      }
      catch (NoProfileCacheEntryException e) {

         log.debug("can't get ParsedProfile for profile id '{}'", report.getHeaderReport().getId());
         componentReport.getScoring().addMessage(Severity.FATAL, "can't get ParsedProfile for profile id '" + report.getHeaderReport().getId() + "'");
         return;

      }
      

      parsedProfile.getComponents().forEach(crc -> {
         componentReport.incrementTotal();
         
         if (crc.isRequired) {
            componentReport.incrementRequired();
         }
         
         componentReport.getComponent(crc).incrementCount();

      });
      
      ConceptReport conceptReport = new ConceptReport();
      report.setConceptReport(conceptReport);
      
      final Map<String, ConceptReport.Concept> conceptMap = new HashMap<String, ConceptReport.Concept>();
      
      parsedProfile
      .getElements()
      .entrySet()
      .stream()
      .filter(entrySet -> entrySet.getKey().startsWith("/cmd:CMD/cmd:Components/"))
      .map(Entry::getValue)
      .forEach(n -> {
         conceptReport.incrementTotal();
         
         if (n.isRequired)
            conceptReport.incrementRequired();

         if (n.concept != null) {
            conceptReport.incrementWithConcept();

            if (n.isRequired) {
               conceptReport.incrementRequired();
            }
            
            conceptMap.computeIfAbsent(n.concept.getUri(), k -> new ConceptReport.Concept(n.concept)).incrementCount();

         }  
      });
      
      conceptReport.getConcepts().addAll(conceptMap.values());

   }

}
