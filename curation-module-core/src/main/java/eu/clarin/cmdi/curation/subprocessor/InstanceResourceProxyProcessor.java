package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.ResProxyReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.ResourceType;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.xml.CMDXPathService;

public class InstanceResourceProxyProcessor extends CMDSubprocessor {

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
		report.resProxyReport = new ResProxyReport();
		
		report.resProxyReport.numOfResProxies = 0;
		report.resProxyReport.numOfResourcesWithMime =  0;
		report.resProxyReport.numOfResProxiesWithReferences = 0;
		report.resProxyReport.resourceType = null;
		
		int numOfTypeResource = 0;
		
		try {
			CMDXPathService xmlService = new CMDXPathService(entity.getPath());
			VTDNav nav = xmlService.reset();
			AutoPilot ap = new AutoPilot(nav);
			ap.selectXPath("/CMD/Resources/ResourceProxyList/ResourceProxy");
			while(ap.evalXPath() != -1){
				report.resProxyReport.numOfResProxies++;
				
				String resType = nav.toElement(VTDNav.FC, "ResourceType")? nav.toNormalizedString(nav.getText()) : null;
				String mimeType = null;
				if(resType != null && resType.equals("Resource")){
					int indMime = nav.getAttrVal("mimetype");
					mimeType= indMime != -1? nav.toNormalizedString(indMime) : null;
					if(mimeType != null && !mimeType.isEmpty())
						report.resProxyReport.numOfResourcesWithMime++;
					numOfTypeResource++;
				}
				
				if(report.resProxyReport.resourceType == null)
					report.resProxyReport.resourceType = new ArrayList<>();
				ResourceType resource = report
						.resProxyReport.resourceType
						.stream().filter(res -> res.type.equals(resType))
						.findFirst()
						.orElse(null);
				if(resource != null)
					resource.count++;
				else{
					resource = new ResourceType();
					resource.type = resType;
					resource.count = 1;
					report.resProxyReport.resourceType.add(resource);
				}
				
				String resProxy = nav.toElement(VTDNav.NS, "ResourceRef")? nav.toNormalizedString(nav.getText()) : null;
				if(resProxy != null && !resProxy.isEmpty())
					report.resProxyReport.numOfResProxiesWithReferences++;
				nav.toElement(VTDNav.PARENT);
			}
			
			report.resProxyReport.percOfResourcesWithMime = numOfTypeResource > 0? 
					(double) report.resProxyReport.numOfResourcesWithMime / numOfTypeResource : 0;
			report.resProxyReport.percOfResProxiesWithReferences = report.resProxyReport.numOfResProxies > 0? 
					(double) report.resProxyReport.numOfResProxiesWithReferences / report.resProxyReport.numOfResProxies : 0;

			
		} catch (Exception e) {
			throw new Exception("Error while processing resource proxies in " + report.fileReport.location, e);
		}
		
		
	}

	@Override
	public Score calculateScore(CMDInstanceReport report) {
		double score = report.resProxyReport.percOfResourcesWithMime + report.resProxyReport.percOfResProxiesWithReferences;
		return new Score(score, 2.0, "cmd-res-proxy", msgs);		
	}

}
