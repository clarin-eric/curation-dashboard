package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.facets.Profile2FacetMap.Facet;
import eu.clarin.cmdi.curation.report.FacetReport;
import eu.clarin.cmdi.curation.report.FacetReport.FacetStruct;

class FacetReportCreator {
	
	private Comparator<FacetStruct> byCoverage = (f1, f2) -> f1.covered? f2.covered? 0 : -1: 1;
	
	public FacetReport createFacetReport(Map<String, Facet> facetMappings){		
		FacetReport facetReport = new FacetReport();
		facetReport.numOfFacets = facetMappings.size();
		facetReport.coveredByProfile = 0;		
		facetReport.facets = new ArrayList<>(facetReport.numOfFacets);
		
		facetMappings.forEach((name, facet) -> {
			FacetStruct facetStruct = new FacetStruct();
			facetStruct.name = name;
			facetStruct.covered = facet != null;
			facetReport.facets.add(facetStruct);
			
			if(facetStruct.covered)
				facetReport.coveredByProfile++;
		});	
		
		
		facetReport.facets = facetReport.facets.stream().sorted(byCoverage).collect(Collectors.toList());
		
		facetReport.profileCoverage = ((double) facetReport.coveredByProfile) / facetReport.numOfFacets;
		
		return facetReport;
		
	}

}
