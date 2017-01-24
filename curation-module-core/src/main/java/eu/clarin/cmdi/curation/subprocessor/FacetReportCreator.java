package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Map;

import eu.clarin.cmdi.curation.facets.Profile2FacetMap.Facet;
import eu.clarin.cmdi.curation.report.FacetReport;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;

class FacetReportCreator {
	
	public FacetReport createFacetReport(Map<String, Facet> facetMappings){		
		FacetReport facetReport = new FacetReport();
		facetReport.numOfFacets = facetMappings.size();
		facetReport.coverage = new ArrayList<>();
		
		facetMappings.keySet().forEach(facetName -> {
			Coverage facet = new Coverage();
			facet.name = facetName;
			facet.coveredByProfile = facetMappings.get(facetName) != null;			
			facetReport.coverage.add(facet);
		});
		
		double numOfCoveredByProfile = facetReport.coverage.stream().filter(f -> f.coveredByProfile).count();
		facetReport.profileCoverage = numOfCoveredByProfile / facetReport.numOfFacets;
		
		return facetReport;		
	}

}
