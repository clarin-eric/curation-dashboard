package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Map;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.FacetReport;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;

class FacetReportCreator {
	
	public FacetReport createFacetReport(ProfileHeader header, FacetMapping facetMapping) throws Exception{		
	    Map<String, CMDINode> elements = new CRService().getParsedProfile(header).getElements();
		FacetReport facetReport = new FacetReport();
		facetReport.numOfFacets = Configuration.FACETS.size();
		facetReport.coverage = new ArrayList<>();
		
		for(String facetName : Configuration.FACETS) {
		    Coverage facet = new Coverage();
            facet.name = facetName;
            facet.coveredByProfile = facetMapping.getFacetConfiguration(facetName).getPatterns().stream().anyMatch(p -> elements.containsKey(p.getPattern())) || 
                    facetMapping.getFacetConfiguration(facetName).getFallbackPatterns().stream().anyMatch(p -> elements.containsKey(p.getPattern()));         
            facetReport.coverage.add(facet);
		}
		
		double numOfCoveredByProfile = facetReport.coverage.stream().filter(f -> f.coveredByProfile).count();
		facetReport.profileCoverage = numOfCoveredByProfile / facetReport.numOfFacets;
		
		return facetReport;		
	}

}
