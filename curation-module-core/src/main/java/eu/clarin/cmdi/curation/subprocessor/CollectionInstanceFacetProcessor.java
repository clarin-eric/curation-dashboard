package eu.clarin.cmdi.curation.subprocessor;

import java.util.List;
import java.util.Map;

import eu.clarin.cmdi.curation.entities.CMDInstance;

import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;
import eu.clarin.cmdi.curation.vlo_extensions.FacetMappingCacheFactory;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;


public class CollectionInstanceFacetProcessor extends CMDSubprocessor {
	

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
	    
	    Map<String, List<ValueSet>> facetValuesMap = entity.getCMDIData().getDocument();

		entity.setParsedInstance(null);
		
	
		FacetMapping facetMapping;
		try{
			facetMapping = FacetMappingCacheFactory.getInstance().getFacetMapping(report.header);
		
		
			report.facets = new FacetReportCreator().createFacetReport(report.header, facetMapping);
			
			int numOfCoveredByIns = 0;
			
			for(Coverage coverage : report.facets.coverage) {
			    if(!coverage.coveredByProfile)
			        continue; 
			    
			    //following lambda expression excludes values set by cross facet mapping
			    coverage.coveredByInstance = (facetValuesMap.get(coverage.name) != null && !facetValuesMap.get(coverage.name).isEmpty() && !facetValuesMap.get(coverage.name).stream().anyMatch(valueSet -> coverage.name.equals(valueSet.getOriginFacetConfig().getName())));
			    if(coverage.coveredByInstance)
			        numOfCoveredByIns++;
			}
			

			report.facets.instanceCoverage = report.facets.numOfFacets == 0? 0.0:(numOfCoveredByIns / (double)report.facets.numOfFacets); //cast to double to get a double as result


		}
		catch (Exception e) {
			throw new Exception("Unable to obtain mapping for " + entity, e);
		}
		finally{
		    entity.setCMDIData(null);
		    entity.setParsedInstance(null);
		};

	}
	
	@Override
	public Score calculateScore(CMDInstanceReport report) {
		return new Score(report.facets.instanceCoverage, 1.0, "facet-mapping", msgs);
	}
}
