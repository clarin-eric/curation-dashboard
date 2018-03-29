package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance.InstanceNode;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.ResProxyReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.ResourceType;
import eu.clarin.cmdi.curation.report.Score;

public class InstanceResourceProxyProcessor extends CMDSubprocessor {

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
		report.resProxyReport = new ResProxyReport();
		
		report.resProxyReport.numOfResProxies = 0;
		report.resProxyReport.numOfResourcesWithMime =  0;
		report.resProxyReport.numOfResProxiesWithReferences = 0;
		report.resProxyReport.resourceType = null;
		
		int numOfTypeResource = 0;
		
		ParsedInstance parsedInstance = entity.getParsedInstance();
		
		final String base = "/CMD/Resources/ResourceProxyList/ResourceProxy";
		
		Collection<InstanceNode> resProxies = parsedInstance.getNodes()
				.stream()
				.filter(n -> n.getXpath().startsWith(base)) 
				.collect(Collectors.toList());
		
		int ind = 1;
		while(true){
			String xpath = base + (ind > 1? "[" + ind + "]" : "") + "/";
			Collection<InstanceNode> singleResProxy = resProxies
					.stream()
					.filter(n -> n.getXpath().startsWith(xpath)) 
					.collect(Collectors.toList());
			
			ind++;			
			
			if(singleResProxy.isEmpty())
				break;
			
			report.resProxyReport.numOfResProxies++;
			
			String resType = null;
			String mimeType = null;
			String reference = null;
			
			for(InstanceNode elem: singleResProxy){
				if(elem.getXpath().equals(xpath + "ResourceType/text()")){
					resType = elem.getValue(); 
				}else if(elem.getXpath().equals(xpath + "ResourceType/@mimetype")){
					mimeType = elem.getValue(); 
				}else if(elem.getXpath().equals(xpath + "ResourceRef/text()")){
					reference = elem.getValue();
				}
			}
			
			if(resType != null && resType.equals("Resource")){
				if(mimeType != null)
					report.resProxyReport.numOfResourcesWithMime++;
				numOfTypeResource++;
			}
			
			if(report.resProxyReport.resourceType == null)
				report.resProxyReport.resourceType = new ArrayList<>();
			
			String resTypeConst = resType;
			if(resType != null){
				ResourceType resource = report
						.resProxyReport.resourceType
						.stream().filter(res -> res.type.equals(resTypeConst))
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
			}
			
			if(reference != null)
				report.resProxyReport.numOfResProxiesWithReferences++;			
		}
		
		report.resProxyReport.percOfResourcesWithMime = numOfTypeResource > 0? 
				(double) report.resProxyReport.numOfResourcesWithMime / numOfTypeResource : 0;
		report.resProxyReport.percOfResProxiesWithReferences = report.resProxyReport.numOfResProxies > 0? 
				(double) report.resProxyReport.numOfResProxiesWithReferences / report.resProxyReport.numOfResProxies : 0;

	}

	@Override
	public Score calculateScore(CMDInstanceReport report) {
		double score = report.resProxyReport.percOfResourcesWithMime + report.resProxyReport.percOfResProxiesWithReferences;
		return new Score(score, 2.0, "cmd-res-proxy", msgs);		
	}

}
