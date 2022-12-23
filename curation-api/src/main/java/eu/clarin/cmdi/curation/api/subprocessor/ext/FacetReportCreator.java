package eu.clarin.cmdi.curation.api.subprocessor.ext;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport.FacetReport;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport.FacetReport.Coverage;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.report.Severity;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FacetReportCreator {

   @Autowired
   private ApiConfig conf;
   @Autowired
   private CRService crService;

   public FacetReport createFacetReport(Score score, ProfileHeader header, FacetsMapping facetMapping) {
      FacetReport facetReport = new FacetReport();


      try {
         final Map<String, CMDINode> elements = crService.getParsedProfile(header).getElements();
         
         facetReport.numOfFacets = conf.getFacets().size();
         facetReport.coverage = new ArrayList<>();
   
         for (String facetName : conf.getFacets()) {
            Coverage facet = new Coverage();
            facet.name = facetName;
            facet.coveredByProfile = facetMapping.getFacetDefinition(facetName).getPatterns().stream()
                  .anyMatch(p -> elements.containsKey(p.getPattern()))
                  || facetMapping.getFacetDefinition(facetName).getFallbackPatterns().stream()
                        .anyMatch(p -> elements.containsKey(p.getPattern()));
            facetReport.coverage.add(facet);
         }
   
         double numOfCoveredByProfile = facetReport.coverage.stream().filter(f -> f.coveredByProfile).count();
         facetReport.profileCoverage = numOfCoveredByProfile / facetReport.numOfFacets;
      }
      catch (NoProfileCacheEntryException e) {
         
         log.debug("no ParsedProfile for profile id '{}'", header.getId());
         score.addMessage(Severity.FATAL, "no ParsedProfile for profile id " + header.getId());

      }

      return facetReport;
   }
}
