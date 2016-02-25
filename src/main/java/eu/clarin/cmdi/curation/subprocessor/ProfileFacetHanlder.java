/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDIProfile;
import eu.clarin.cmdi.curation.facets.FacetConceptMappingService;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap;
import eu.clarin.cmdi.curation.report.CMDIProfileReport;
import eu.clarin.cmdi.curation.report.FacetReport;
import eu.clarin.cmdi.curation.report.FacetReport.Profile;

/**
 * @author dostojic
 *
 */
public class ProfileFacetHanlder extends ProcessingStep<CMDIProfile, CMDIProfileReport> {

    private static final Logger _logger = LoggerFactory.getLogger(ProfileFacetHanlder.class);

    @Override
    public boolean process(CMDIProfile entity, CMDIProfileReport report) {

	FacetConceptMappingService service;
	Profile2FacetMap profileMap;
	try {
	    service = FacetConceptMappingService.getInstance();
	    profileMap = service.getMapping(report.ID);
	} catch (Exception e) {
	    _logger.error("Unable to create facet mapping for profile {}", report.ID, e);
	    return false;
	}

	int totalNumOfFacets = service.getTotalNumOfFacets();

	Profile profileReport = new Profile();
	profileReport.numOfCoveredFacets = profileMap.getMappings().size();
	profileReport.notCovered = profileMap.getNotCovered();
	profileReport.coverage = 1.0 * profileReport.numOfCoveredFacets / totalNumOfFacets;
	
	report.facet = new FacetReport();
	report.facet.numOfFacets = totalNumOfFacets;
	report.facet.profile = profileReport;
	report.facet.messages = msgs;

	return true;
    }

}
