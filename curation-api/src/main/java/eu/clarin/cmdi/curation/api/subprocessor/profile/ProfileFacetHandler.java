/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.profile;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileFacetReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileFacetReport.Coverage;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * The type Profile facet handler.
 */
@Component
@Slf4j
public class ProfileFacetHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport> {
   @Autowired
   private ApiConfig conf;
   @Autowired
   private CRService crService;
   /**
    * The Fac.
    */
   @Autowired
   FacetsMappingCacheFactory fac;

   /**
    * Process.
    *
    * @param profile the profile
    * @param report  the report
    */
   public void process(CMDProfile profile, CMDProfileReport report) throws MalFunctioningProcessorException {
      
      report.facetReport = new ProfileFacetReport();

      try {

         FacetsMapping facetMapping = fac.getFacetsMapping(profile.getSchemaLocation());

         final Map<String, CMDINode> elements = crService.getParsedProfile(profile.getSchemaLocation()).xpathElementNode();
         
   
         for (String facetName : conf.getFacets()) {
            report.facetReport.numOfFacets++;
            
            Coverage coverage = new Coverage(facetName);
            report.facetReport.coverages.add(coverage);
            
            if(facetMapping.getFacetDefinition(facetName).getPatterns().stream()
                  .anyMatch(p -> elements.containsKey(p.getPattern()))
                  || facetMapping.getFacetDefinition(facetName).getFallbackPatterns().stream()
                        .anyMatch(p -> elements.containsKey(p.getPattern()))){
               coverage.coveredByProfile = true;
               report.facetReport.numOfFacetsCoveredByProfile++;
            }
         }                  
      }
      catch (NoProfileCacheEntryException e) {
         
         log.debug("no ParsedProfile for profile location '{}'", profile.getSchemaLocation());
         report.details.add(new Detail(Severity.FATAL,"facet" , "no ParsedProfile for profile location " + profile.getSchemaLocation()));

      }
      catch (CRServiceStorageException | CCRServiceNotAvailableException e) {
          throw new MalFunctioningProcessorException(e);
      }
       report.facetReport.percProfileCoverage = (double) report.facetReport.numOfFacetsCoveredByProfile/report.facetReport.numOfFacets;
      report.facetReport.score = report.facetReport.percProfileCoverage;
      report.score+=report.facetReport.score;
   }
}
