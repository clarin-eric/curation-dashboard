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
public class ProfileConceptHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport> {

   @Autowired
   private CRService crService;

   public void process(CMDProfile profile, CMDProfileReport report) {
      
      report.componentReport = new ComponentReport();

      
      ParsedProfile parsedProfile = null;

      try {
         parsedProfile = crService.getParsedProfile(report.headerReport.getProfileHeader());
      }
      catch (NoProfileCacheEntryException e) {

         log.debug("can't get ParsedProfile for profile id '{}'", report.headerReport.getId());
         return;

      }
      
      Map<String, ComponentReport.Component> componentMap = new HashMap<String, ComponentReport.Component>();

      parsedProfile.getComponents().forEach(crc -> {
         report.componentReport.total++;
         
         if (crc.isRequired) {
            report.componentReport.required++;
         }
         
         componentMap
            .computeIfAbsent(crc.component.id, k -> new ComponentReport.Component(crc.component.id, crc.component.name))
            .count++;

      });
           
      report.componentReport.unique = componentMap.size();
      
      report.componentReport.components = componentMap.values();
      
      report.conceptReport =  new ConceptReport();
      
      final Map<String, ConceptReport.Concept> conceptMap = new HashMap<String, ConceptReport.Concept>();
      
      parsedProfile
      .getElements()
      .entrySet()
      .stream()
      .filter(entrySet -> entrySet.getKey().startsWith("/cmd:CMD/cmd:Components/"))
      .map(Entry::getValue)
      .forEach(n -> {
         report.conceptReport.total++;
         
         if (n.isRequired)
            report.conceptReport.required++;

         if (n.concept != null) {
            report.conceptReport.withConcept++;

       
            conceptMap.computeIfAbsent(n.concept.getUri(), k -> new ConceptReport.Concept(n.concept)).count++;

         }  
      });
      
      report.conceptReport.unique = conceptMap.size();
      report.conceptReport.percWithConcept = (report.conceptReport.total!=0?(double) report.conceptReport.withConcept/report.conceptReport.total:0.0);
      report.conceptReport.concepts = conceptMap.values();
      
      report.conceptReport.scoring.maxScore = 1.0;
      report.conceptReport.scoring.score = report.conceptReport.percWithConcept;
   }
}
