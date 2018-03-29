/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.util.Map;

import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.facets.FacetConceptMappingService;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap.Facet;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Score;

/**
 * @author dostojic
 *
 */
public class ProfileFacetHanlder extends ProcessingStep<CMDProfile, CMDProfileReport> {

    @Override
    public void process(CMDProfile entity, CMDProfileReport report) throws Exception {

		FacetConceptMappingService service = new FacetConceptMappingService();	
		Map<String, Facet> facetMappings;
		try {
			facetMappings = service.getFacetMapping(report.header).getMappings();
		} catch (Exception e) {
			throw new Exception("Unable to create facet mapping for " + entity.toString(), e);
		}
		
		report.facet = new FacetReportCreator().createFacetReport(facetMappings);

	}
	
	@Override
	public Score calculateScore(CMDProfileReport report) {
		return new Score(report.facet.profileCoverage, 1.0, "facets-section", msgs);
	}

}
