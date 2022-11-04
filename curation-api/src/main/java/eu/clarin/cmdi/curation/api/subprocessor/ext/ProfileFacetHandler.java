/**
 *
 */
package eu.clarin.cmdi.curation.api.subprocessor.ext;

import org.springframework.beans.factory.annotation.Autowired;

import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractMessageCollection;
import eu.clarin.cmdi.curation.api.vlo_extensions.FacetsMappingCacheFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;

public class ProfileFacetHandler extends AbstractMessageCollection {
   
   @Autowired
   FacetsMappingCacheFactory fac;

   public void process(CMDProfileReport report) throws SubprocessorException {

      FacetsMapping facetMapping = null;

      facetMapping = fac.getFacetsMapping(report.header);

      report.facet = new FacetReportCreator().createFacetReport(report.header, facetMapping);

   }

   public Score calculateScore(CMDProfileReport report) {
      return new Score(report.facet.profileCoverage, 1.0, "facets-section", this.getMessages());
   }

}
