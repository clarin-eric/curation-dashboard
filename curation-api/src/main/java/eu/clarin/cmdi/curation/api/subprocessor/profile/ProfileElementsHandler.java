/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.Concept;
import eu.clarin.cmdi.curation.api.report.Score;
//import eu.clarin.cmdi.curation.api.report.CMDProfileReport.Component;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport.Components;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport.Concepts;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport.Elements;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractMessageCollection;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import lombok.extern.slf4j.Slf4j;

/**

 *
 */
@Slf4j
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProfileElementsHandler extends AbstractMessageCollection {

   @Autowired
   private CRService crService;

   public void process(CMDProfileReport report) throws SubprocessorException {
      ParsedProfile parsedProfile = null;

      try {
         parsedProfile = crService.getParsedProfile(report.header);
      }
      catch (NoProfileCacheEntryException e) {

         log.debug("can't get ParsedProfile for profile id '{}'", report.header.getId());
         throw new SubprocessorException();
      }

      report.components = createComponentSegment(parsedProfile);
      report.elements = createElementSegment(parsedProfile);

   }

   public Score calculateScore(CMDProfileReport report) {
      double score = report.elements.percWithConcept;
      return new Score(score, 1.0, "cmd-concepts-section", this.getMessages());
   }

   private Components createComponentSegment(ParsedProfile parsedProfile) {
      Collection<CMDINode> crComponents = parsedProfile.getComponents();
      Components comp = new Components();
      comp.total = crComponents.size();
      comp.required = 0;
      comp.unique = 0;
      comp.components = new ArrayList<eu.clarin.cmdi.curation.api.report.CMDProfileReport.Component>();

      crComponents.forEach(crc -> {

         if (crc.isRequired)
            comp.required++;

         eu.clarin.cmdi.curation.api.report.CMDProfileReport.Component component = comp.components.stream()
               .filter(c -> c.id.equals(crc.component.id)).findFirst().orElse(null);
         if (component != null) {
            component.count++;
         }
         else {
            component = new eu.clarin.cmdi.curation.api.report.CMDProfileReport.Component();
            component.id = crc.component.id;
            component.name = crc.component.name;
            component.count = 1;
            comp.components.add(component);
         }
      });

      comp.unique = comp.components.size();
      return comp;
   }

   private Elements createElementSegment(ParsedProfile parsedProfile) {
      Collection<CMDINode> elemNodes = parsedProfile.getElements().entrySet().stream() // dont consider elements from
                                                                                       // header and resources, they
                                                                                       // dont have concept
            .filter(e -> e.getKey().startsWith("/cmd:CMD/cmd:Components/")).map(Entry::getValue)
            .collect(Collectors.toList());
      Elements elems = new Elements();
      elems.total = elemNodes.size();
      elems.required = 0;
      elems.withConcept = 0;
      elems.concepts = new Concepts();
      elems.concepts.total = 0;
      elems.concepts.unique = 0;
      elems.concepts.required = 0;
      elems.concepts.concepts = new ArrayList<>();

      elemNodes.forEach(n -> {
         if (n.isRequired)
            elems.required++;

         if (n.concept != null) {
            elems.withConcept++;

            if (n.isRequired)
               elems.concepts.required++;

            Concept concept = elems.concepts.concepts.stream().filter(c -> c.uri.equals(n.concept.uri)).findFirst()
                  .orElse(null);

            if (concept != null) {
               concept.count++;
            }
            else {
               elems.concepts.concepts.add(new Concept(n.concept.uri, n.concept.prefLabel, n.concept.status));
            }
         }

      });

      elems.concepts.total = elems.withConcept;
      elems.concepts.unique = elems.concepts.concepts.size();
      elems.percWithConcept = elems.total == 0.0 ? 0.0 : ((double) elems.withConcept) / elems.total;

      return elems;

   }

}
