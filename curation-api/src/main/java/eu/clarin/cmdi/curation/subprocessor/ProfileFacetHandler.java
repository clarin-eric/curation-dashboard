/**
 *
 */
package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.exception.SubprocessorException;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.vlo_extensions.FacetsMappingCacheFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;

public class ProfileFacetHandler extends AbstractMessageCollection {

   public void process(CMDProfileReport report) throws SubprocessorException {

      FacetsMapping facetMapping = null;

      facetMapping = FacetsMappingCacheFactory.getInstance().getFacetsMapping(report.header);

      report.facet = new FacetReportCreator().createFacetReport(report.header, facetMapping);

   }

   public Score calculateScore(CMDProfileReport report) {
      return new Score(report.facet.profileCoverage, 1.0, "facets-section", this.getMessages());
   }

}
