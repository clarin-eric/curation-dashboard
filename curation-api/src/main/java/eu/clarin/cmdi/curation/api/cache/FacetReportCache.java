/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.cache;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.report.Scoring.Severity;
import eu.clarin.cmdi.curation.api.report.profile.section.ProfileFacetReport;
import eu.clarin.cmdi.curation.api.report.profile.section.ProfileFacetReport.Coverage;
import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Component
@Slf4j
public class FacetReportCache {
   
   @Autowired
   private ApiConfig conf;
   @Autowired
   private CRService crService;
   @Autowired
   FacetsMappingCacheFactory fac;
   
   @Cacheable(key = "#header.id", condition = "#header.isPublic")
   public ProfileFacetReport getFacetReport(ProfileHeader header) {
      
      ProfileFacetReport facetReport = new ProfileFacetReport();
      
      FacetsMapping facetMapping = null;

      facetMapping = fac.getFacetsMapping(header);

      try {
         final Map<String, CMDINode> elements = crService.getParsedProfile(header).getElements();
         
         facetReport.setNumOfFacets(conf.getFacets().size());
   
         for (String facetName : conf.getFacets()) {
            Coverage facet = new Coverage();
            facet.setName(facetName);
            facet.setCoveredByProfile(facetMapping.getFacetDefinition(facetName).getPatterns().stream()
                  .anyMatch(p -> elements.containsKey(p.getPattern()))
                  || facetMapping.getFacetDefinition(facetName).getFallbackPatterns().stream()
                        .anyMatch(p -> elements.containsKey(p.getPattern())));
            facetReport.getCoverage().add(facet);
         }
      }
      catch (NoProfileCacheEntryException e) {
         
         log.debug("no ParsedProfile for profile id '{}'", header.getId());
         facetReport.getScore().addMessage(Severity.FATAL, "no ParsedProfile for profile id " + header.getId());

      }
      
      return facetReport;      
   }
}
