package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;

import eu.clarin.cmdi.curation.configuration.CurationConfig;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.cr.cache.CRServiceImpl;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.report.FacetReport;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;

class FacetReportCreator {
   
   @Autowired
   private CurationConfig conf;
	
	public FacetReport createFacetReport(ProfileHeader header, FacetsMapping facetMapping) throws ExecutionException {
	    Map<String, CMDINode> elements = new CRServiceImpl().getParsedProfile(header).getElements();
		FacetReport facetReport = new FacetReport();
		facetReport.numOfFacets = conf.getFacets().size();
		facetReport.coverage = new ArrayList<>();
		
		for(String facetName : conf.getFacets()) {
		    Coverage facet = new Coverage();
            facet.name = facetName;
            facet.coveredByProfile = facetMapping.getFacetDefinition(facetName).getPatterns().stream().anyMatch(p -> elements.containsKey(p.getPattern())) || 
                    facetMapping.getFacetDefinition(facetName).getFallbackPatterns().stream().anyMatch(p -> elements.containsKey(p.getPattern()));         
            facetReport.coverage.add(facet);
		}
		
		double numOfCoveredByProfile = facetReport.coverage.stream().filter(f -> f.coveredByProfile).count();
		facetReport.profileCoverage = numOfCoveredByProfile / facetReport.numOfFacets;
		
		return facetReport;		
	}

}
