/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.subprocessor.ext.FacetReportCreator;

@Component
public class ProfileFacetHandler extends AbstractSubprocessor<CMDProfile, CMDProfileReport> {

   @Autowired
   FacetReportCreator facetReportCreator;

   public void process(CMDProfile profile, CMDProfileReport report) {

      facetReportCreator.createFacetReport(report.headerReport.getHeader(), report.facetReport);
      
   }
}
