/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.facets.FacetMappingCacheFactory;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;

/**
 * @author dostojic, wowasa
 *
 */
public class ProfileFacetHanlder extends ProcessingStep<CMDProfile, CMDProfileReport> {

    @Override
    public void process(CMDProfile entity, CMDProfileReport report) throws Exception {

	
		FacetMapping facetMapping;
		try {
			facetMapping = FacetMappingCacheFactory.getInstance().getFacetMapping(report.header);
		} catch (Exception e) {
			throw new Exception("Unable to create facet mapping for " + entity.toString(), e);
		}
		
		report.facet = new FacetReportCreator().createFacetReport(report.header, facetMapping);

	}
	
	@Override
	public Score calculateScore(CMDProfileReport report) {
		return new Score(report.facet.profileCoverage, 1.0, "facets-section", msgs);
	}

}
