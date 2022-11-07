/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractMessageCollection;
import eu.clarin.cmdi.curation.api.subprocessor.ext.FacetReportCreator;
import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;

@Component
public class ProfileFacetHandler extends AbstractMessageCollection {
   
   @Autowired
   FacetsMappingCacheFactory fac;
   @Autowired
   FacetReportCreator facetReportCreator;

   public synchronized void process(CMDProfileReport report) throws SubprocessorException {

      FacetsMapping facetMapping = null;

      facetMapping = fac.getFacetsMapping(report.header);

      report.facet = facetReportCreator.createFacetReport(report.header, facetMapping);

   }

   public synchronized Score calculateScore(CMDProfileReport report) {
      return new Score(report.facet.profileCoverage, 1.0, "facets-section", this.getMessages());
   }
}
