package eu.clarin.cmdi.curation.api.subprocessor.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport.ResProxyReport;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport.ResourceType;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

@Component
public class CollectionInstanceResourceProxyProcessor extends AbstractSubprocessor {

	@Override
	public synchronized void process(CMDInstance entity, CMDInstanceReport report) {
	    CMDIData<Map<String, List<ValueSet>>> cmdiData = entity.getCmdiData();
	    
		report.resProxyReport = new ResProxyReport();
		
		report.resProxyReport.numOfResProxies = 0;
		report.resProxyReport.numOfResourcesWithMime =  0;
		report.resProxyReport.numOfResProxiesWithReferences = 0;
		report.resProxyReport.resourceType = null;
		
		int numOfTypeResource = cmdiData.getDataResources().size();
		
		for(Resource resource : cmdiData.getDataResources()) {
		    if(resource.getMimeType() != null)
		        report.resProxyReport.numOfResourcesWithMime++;
		    if(resource.getResourceName() != null && !resource.getResourceName().isEmpty())
		        report.resProxyReport.numOfResProxiesWithReferences++;
		    
		}
		
		
		addResourceType(cmdiData.getLandingPageResources(), report);
		addResourceType(cmdiData.getMetadataResources(), report);
		addResourceType(cmdiData.getSearchPageResources(), report);
		addResourceType(cmdiData.getSearchResources(), report);
			
			report.resProxyReport.percOfResourcesWithMime = numOfTypeResource > 0? 
					(double) report.resProxyReport.numOfResourcesWithMime / numOfTypeResource : 0;
			report.resProxyReport.percOfResProxiesWithReferences = report.resProxyReport.numOfResProxies > 0? 
					(double) report.resProxyReport.numOfResProxiesWithReferences / report.resProxyReport.numOfResProxies : 0;

		
	}
	
	private void addResourceType(List<Resource> resources, CMDInstanceReport report) {
	    if(resources.isEmpty())
	        return;
	    if(report.resProxyReport.resourceType == null)
            report.resProxyReport.resourceType = new ArrayList<>();
	    
	    ResourceType resourceType = new ResourceType();
	    resourceType.type = resources.get(0).getType();
	    resourceType.count = resources.size();
	    
	    for(Resource resource : resources) {
	           if(resource.getResourceName() != null && !resource.getResourceName().isEmpty())
	                report.resProxyReport.numOfResProxiesWithReferences++;
	    }
	    
	    report.resProxyReport.resourceType.add(resourceType);
	    
	}

	@Override
	public synchronized Score calculateScore(CMDInstanceReport report) {
		double score = report.resProxyReport.percOfResourcesWithMime + report.resProxyReport.percOfResProxiesWithReferences;
		return new Score(score, 2.0, "cmd-res-proxy", this.getMessages());		
	}

}
