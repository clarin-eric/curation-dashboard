package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.facets.FacetMappingCacheFactory;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance.InstanceNode;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Concept;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;
import eu.clarin.cmdi.curation.report.FacetReport.FacetValueStruct;
import eu.clarin.cmdi.curation.report.FacetReport.ValueNode;
import eu.clarin.cmdi.curation.xml.CMDXPathService;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;

import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;



/**
 * @author dostojic
 *
 */
public class InstanceFacetProcessor extends CMDSubprocessor {

	private final static Logger _logger = LoggerFactory.getLogger(InstanceFacetProcessor.class);
	private static CMDIParserVTDXML _parser;

	int numOfFacetsCoveredByIns = 0;
	
    static{
        
        VloConfig vloConfig = null;
        FieldNameService fieldNameService = null;
        LanguageCodeUtils languageCodeUtils = null;
        try {
            vloConfig = new DefaultVloConfigFactory().newConfig();
            fieldNameService = new FieldNameServiceImpl(vloConfig);
            languageCodeUtils = new LanguageCodeUtils(vloConfig);     
            
            _parser = new CMDIParserVTDXML(
                    MetadataImporter.registerPostProcessors(vloConfig, fieldNameService, languageCodeUtils),
                    vloConfig,
                    FacetMappingCacheFactory.getInstance(), 
                    null,
                    false
                );
 
            
            
        } 
        catch (IOException ex) {

            _logger.error("couldn't instantiate CMDIParserVTDXML!", ex);

        }   

    }



	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
				
		
        //parse instance
        CMDXPathService xmlService = new CMDXPathService(entity.getPath());
        
        VTDNav nav = xmlService.getNavigator();
 
		
		try{

						
			
			
		     Map<Integer, ValueNode> nodesMap = getValueNodesMap(entity, report, nav);
		     
		     _logger.trace("nodes map: \n{}", nodesMap);
		     
		     facetsToNodes(report, nodesMap, nav);


			
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
	
	private void facetsToNodes(CMDInstanceReport report, Map<Integer, ValueNode> nodesMap, VTDNav nav) throws Exception {

	    FacetMapping facetMapping = FacetMappingCacheFactory.getInstance().getFacetMapping(report.header);
	    
	    Map<FacetConfiguration, List<ValueSet>> facetValuesMap = _parser.getFacetValuesMap(null, nav, facetMapping);
	    

	    
	    //regrouping by VDTIndex, target facet configuration
	    Map<Integer, Map<FacetConfiguration,List<ValueSet>>> indexValuesMap = new HashMap<Integer, Map<FacetConfiguration,List<ValueSet>>>();
	    

	    for(Coverage coverage : report.facets.coverage) {
	        List<ValueSet> values = facetValuesMap.get(facetMapping.getFacetConfiguration(coverage.name));
	        
	        if(values != null) {
	            values.forEach(value -> {
	                indexValuesMap.computeIfAbsent(value.getVtdIndex(), k -> new HashMap<FacetConfiguration,List<ValueSet>>()).computeIfAbsent(value.getOriginFacetConfig(), v -> new ArrayList<ValueSet>()).add(value);
	            });
	            
	        }
	    }
	    
	    report.facets = new FacetReportCreator().createFacetReport(report.header, facetMapping);  
	    
	    indexValuesMap.forEach((index, reducedFacetValueMap) -> {
	        ValueNode node = nodesMap.get(index);
            
            if(node != null) {  //might be null if the node has no value
                node.facet = new ArrayList<FacetValueStruct>();
                
                reducedFacetValueMap.forEach((facetConfig, values) -> {
                    node.facet.add(createFacetValueStruct(facetConfig.getName(), node.value, values.stream().map(valueSet -> valueSet.getValueLanguagePair().getLeft()).collect(Collectors.joining(" ;")), false));
                });
                //
            }

	        
	    });        
	}

	
	@Override
	public Score calculateScore(CMDInstanceReport report) {
		return new Score(report.facets.instanceCoverage, 1.0, "facet-mapping", msgs);
	}

	/*
	 * 
	 */
	
	private FacetValueStruct createFacetValueStruct(String facetName, String value, String normalizedValue, boolean isDerived){
		
		FacetValueStruct facetNode = new FacetValueStruct();
		facetNode.name = facetName;
		facetNode.isDerived = isDerived? true : null;
		facetNode.normalisedValue = null;
		

		

		if(normalizedValue != null && !normalizedValue.trim().isEmpty() && !normalizedValue.equals("--") && !value.equals(normalizedValue)){
			addMessage(Severity.INFO, "Normalised value for facet " + facetName + ": '" + value + "' into '" + normalizedValue + "'");
			facetNode.normalisedValue = normalizedValue;
		}
			
		return facetNode;	
		
	}
}
