package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance.InstanceNode;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Concept;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;
import eu.clarin.cmdi.curation.report.FacetReport.FacetValueStruct;
import eu.clarin.cmdi.curation.report.FacetReport.ValueNode;
import eu.clarin.cmdi.curation.vlo_extensions.FacetMappingCacheFactory;
import eu.clarin.cmdi.curation.xml.CMDXPathService;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;

import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;



/**
 * @author dostojic/wowasa
 *
 */
public class InstanceFacetProcessor extends CMDSubprocessor {

	private final static Logger _logger = LoggerFactory.getLogger(InstanceFacetProcessor.class);


	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
				
		
        //parse instance
        CMDXPathService xmlService = new CMDXPathService(entity.getPath());
        
        VTDNav nav = xmlService.getNavigator();
 
		
		try{

						
			
			
		     Map<Integer, ValueNode> nodesMap = getValueNodesMap(entity, report, nav);
		     
		     _logger.trace("nodes map: \n{}", nodesMap);
		     
		     facetsToNodes(entity, report, nodesMap, nav);


			
			report.facets.values = nodesMap.values();
			
			double numOfCoveredByIns = report.facets.coverage.stream().filter(facet -> facet.coveredByInstance).count();
			report.facets.instanceCoverage = numOfCoveredByIns / report.facets.numOfFacets;

		}
		catch (Exception e) {
			_logger.trace("", e);
			throw new Exception("Unable to obtain mapping for " + entity, e);
		};

	}
	
	private Map<Integer, ValueNode> getValueNodesMap(CMDInstance entity, CMDInstanceReport report, VTDNav nav) throws Exception{
	    Map<Integer, ValueNode> nodesMap = new LinkedHashMap<Integer, ValueNode>();
	    
	    Map<String, CMDINode> elements = new CRService().getParsedProfile(report.header).getElements();
        ParsedInstance parsedInstance = entity.getParsedInstance();

	    
	    
	    AutoPilot ap = new AutoPilot(nav);  
         
         //create value nodes
         for(InstanceNode instanceNode : parsedInstance.getNodes()) {
             if(!instanceNode.getValue().isEmpty()) {
                 ValueNode val = new ValueNode();
                 val.xpath = instanceNode.getXpath();
                 val.value = instanceNode.getValue();
                 
                 CMDINode node = elements.get(instanceNode.getXpath());
                 if(node != null && node.concept != null)
                     val.concept = new Concept(node.concept.uri, node.concept.prefLabel, node.concept.status);
                 
                 //determines the index for each xpath
                 ap.selectXPath(instanceNode.getXpath().replaceAll("\\w+:", ""));
                 nodesMap.put(ap.evalXPath(), val);
                 ap.resetXPath();                    
             }
         }
         
         return nodesMap;
	}
	
	private void facetsToNodes(CMDInstance entity, CMDInstanceReport report, Map<Integer, ValueNode> nodesMap, VTDNav nav) throws Exception {

	    FacetMapping facetMapping = FacetMappingCacheFactory.getInstance().getFacetMapping(report.header);	   

	    
	    Map<String, List<ValueSet>> facetValuesMap = entity.getCMDIData().getDocument();
	    

	    report.facets = new FacetReportCreator().createFacetReport(report.header, facetMapping);  
	    
	    int numOfCoveredByIns = 0;
	    

	    for(Coverage coverage : report.facets.coverage) {
	        List<ValueSet> values = facetValuesMap.get(coverage.name);
	        
           if(values == null) //no values from instance for this facet
                continue;
           
           coverage.coveredByInstance = true; //one or more values for the specific facet
           numOfCoveredByIns++;
           
           //in the next step the value(s) have to be mapped to the right node
	        
	        Map<Integer, List<ValueSet>> facetMap = values.stream().collect(Collectors.groupingBy(ValueSet::getVtdIndex)); //groups valueSets by vtdIndex
	        

	        
	        for(Map.Entry<Integer, List<ValueSet>> entry : facetMap.entrySet()) {
	            
	            if(nodesMap.containsKey(entry.getKey())){ //there is a node where the xpath has the same vtdIndex
	                ValueNode node = nodesMap.get(entry.getKey());
	                
	                //initializing node.facet if not done already:
	                if(node.facet == null)
	                    node.facet = new ArrayList<FacetValueStruct>();
	                
	                node.facet.add(
	                        createFacetValueStruct(
	                                coverage.name, 
	                                node.value, 
	                                entry.getValue().stream().map(valueSet -> valueSet.getValueLanguagePair().getLeft()).collect(Collectors.joining(" ;")), 
	                                //entry.getValue().get(0).isDerived() //assumes that a facet isn't defined as origin and derived at the same time
	                                entry.getValue().stream().anyMatch(ValueSet::isDerived), //assumes that a facet isn't defined as origin and derived at the same time
	                                entry.getValue().stream().anyMatch(ValueSet::isResultOfValueMapping) //assumes that a facet isn't defined as origin and derived at the same time
                                )
                        );
	                
	            }
	            
	        }

	    }
	    
	    report.facets.instanceCoverage = report.facets.numOfFacets == 0? 0.0:(numOfCoveredByIns / (double)report.facets.numOfFacets); //cast to double to get a double as result

	}

	
	@Override
	public Score calculateScore(CMDInstanceReport report) {
		return new Score(report.facets.instanceCoverage, 1.0, "facet-mapping", msgs);
	}

	/*
	 * 
	 */
	
	private FacetValueStruct createFacetValueStruct(String facetName, String value, String normalizedValue, boolean isDerived, boolean usesValueMapping){
		
		FacetValueStruct facetNode = new FacetValueStruct();
		facetNode.name = facetName;
		facetNode.isDerived = isDerived? true : null;
		facetNode.usesValueMapping = usesValueMapping? true : null;
		facetNode.normalisedValue = value.equals(normalizedValue)?null:normalizedValue;
		

		

		if(normalizedValue != null && !normalizedValue.trim().isEmpty() && !normalizedValue.equals("--") && !value.equals(normalizedValue)){
			addMessage(Severity.INFO, "Normalised value for facet " + facetName + ": '" + value + "' into '" + normalizedValue + "'");
			facetNode.normalisedValue = normalizedValue;
		}
			
		return facetNode;	
		
	}
}
