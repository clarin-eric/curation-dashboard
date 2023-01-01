/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.cache.FacetReportCache;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.pph.ProfileHeader;


@Component
public class ProfileFacetHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport> {

   @Autowired
   private CRService crService;
   @Autowired
   FacetReportCache facetReportCache;

   public void process(CMDProfile profile, CMDProfileReport report) {
      
      ProfileHeader header = crService.createProfileHeader(profile.getSchemaLocation(), "1.x", false);

      report.setFacetReport(facetReportCache.getFacetReport(header));
      
   }
}
