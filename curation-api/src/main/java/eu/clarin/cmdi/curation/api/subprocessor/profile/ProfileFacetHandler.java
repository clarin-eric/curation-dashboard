/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.subprocessor.ext.FacetReportCreator;
import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;

@Component
public class ProfileFacetHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport> {
   
   @Autowired
   FacetsMappingCacheFactory fac;
   @Autowired
   FacetReportCreator facetReportCreator;

   public void process(CMDProfile profile, CMDProfileReport report) {
      
      Score score = new Score("facets-section", 1.0);

      FacetsMapping facetMapping = null;

      facetMapping = fac.getFacetsMapping(report.header);

      report.facet = facetReportCreator.createFacetReport(score, report.header, facetMapping);
      
      score.setScore(report.facet.profileCoverage);
      report.addSegmentScore(score);
   }
}
