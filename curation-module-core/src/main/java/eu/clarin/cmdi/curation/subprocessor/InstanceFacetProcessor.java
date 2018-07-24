package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.entities.CMDInstance;

import eu.clarin.cmdi.curation.facets.FacetConstants;
import eu.clarin.cmdi.curation.facets.FacetMappingCacheFactory;
import eu.clarin.cmdi.curation.facets.postprocessor.AvailabilityPostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.ContinentNamePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.CountryNamePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.LanguageCodePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.LanguageNamePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.LicensePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.NationalProjectPostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.OrganisationPostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.PostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.ResourceClassPostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.TemporalCoveragePostProcessor;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.instance_parser.ParsedInstance.InstanceNode;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Concept;
import eu.clarin.cmdi.curation.report.FacetReport;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;
import eu.clarin.cmdi.curation.report.FacetReport.FacetValueStruct;
import eu.clarin.cmdi.curation.report.FacetReport.ValueNode;
import eu.clarin.cmdi.curation.xml.CMDXPathService;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizerWithVocabularyMap;
import eu.clarin.cmdi.vlo.importer.normalizer.AvailabilityPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.CMDIComponentProfileNamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.ContinentNamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.CountryNamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.IdPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.LanguageCodePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.LanguageNamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.LicensePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.LicenseTypePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.NamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.OrganisationPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.ResourceClassPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.TemporalCoveragePostNormalizer;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * @author dostojic
 *
 */
public class InstanceFacetProcessor extends CMDSubprocessor {

	private final static Logger _logger = LoggerFactory.getLogger(InstanceFacetProcessor.class);
	private static final Map<String, AbstractPostNormalizer> _postNormalizers;

	int numOfFacetsCoveredByIns = 0;
	
    static{
        ImmutableMap.Builder<String, AbstractPostNormalizer> imb = ImmutableMap.builder();

        try {
            VloConfig vloConfig = new DefaultVloConfigFactory().newConfig();
            FieldNameService fieldNameService = new FieldNameServiceImpl(vloConfig);
            LanguageCodeUtils languageCodeUtils = new LanguageCodeUtils(vloConfig);

            
            
            imb.put(fieldNameService.getFieldName(FieldKey.ID), new IdPostNormalizer());
            
            if(fieldNameService.getFieldName(FieldKey.CONTINENT) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.CONTINENT), new ContinentNamePostNormalizer());
            if(fieldNameService.getFieldName(FieldKey.COUNTRY) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.COUNTRY), new CountryNamePostNormalizer(vloConfig));
            if(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE), new LanguageCodePostNormalizer(vloConfig, languageCodeUtils));
            if(fieldNameService.getFieldName(FieldKey.LANGUAGE_NAME) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.LANGUAGE_NAME), new LanguageNamePostNormalizer(languageCodeUtils));
            if(fieldNameService.getFieldName(FieldKey.AVAILABILITY) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.AVAILABILITY), new AvailabilityPostNormalizer(vloConfig));
            if(fieldNameService.getFieldName(FieldKey.LICENSE_TYPE) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.LICENSE_TYPE), new LicenseTypePostNormalizer(vloConfig));
            if(fieldNameService.getFieldName(FieldKey.ORGANISATION) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.ORGANISATION), new OrganisationPostNormalizer(vloConfig));
            if(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE), new TemporalCoveragePostNormalizer());
            if(fieldNameService.getFieldName(FieldKey.CLARIN_PROFILE) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.CLARIN_PROFILE), new CMDIComponentProfileNamePostNormalizer(vloConfig));
            if(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS), new ResourceClassPostNormalizer());
            if(fieldNameService.getFieldName(FieldKey.LICENSE) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.LICENSE), new LicensePostNormalizer(vloConfig));
            if(fieldNameService.getFieldName(FieldKey.NAME) != null)
                imb.put(fieldNameService.getFieldName(FieldKey.NAME), new NamePostNormalizer());
            


                    
                    
        } 
        catch (IOException ex) {
            // TODO Auto-generated catch block
            _logger.error("couldn't initialisze facet mapping cache!", ex);

        }   

        _postNormalizers = imb.build();
    }



	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
				
		Map<String, CMDINode> elements = new CRService().getParsedProfile(report.header).getElements();
		ParsedInstance parsedInstance = entity.getParsedInstance();
		FacetMapping facetMapping;
		
        //parse instance
        CMDXPathService xmlService = new CMDXPathService(entity.getPath());
        
        VTDNav nav = xmlService.getNavigator();
        
        AutoPilot ap = new AutoPilot(nav);  
        ap.declareXPathNameSpace("cmd", "http://www.clarin.eu/cmd/1");
        ap.declareXPathNameSpace("cmdp", "http://www.clarin.eu/cmd/1/profiles/" + report.header.id);  

 
		
		try{
			facetMapping = FacetMappingCacheFactory.getInstance().getFacetMapping(report.header);
						
			report.facets = new FacetReportCreator().createFacetReport(facetMapping);	
			
		     Map<Integer, ValueNode> nodesMap = new HashMap<Integer, ValueNode>();
		     
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
		             ap.selectXPath(instanceNode.getXpath());
		             nodesMap.put(ap.evalXPath(), val);
		             ap.resetXPath();		             
		         }
		     }
		     
		     //Map<FacetConfiguration,List<Pair<String,String>>> facetValueMap = new HashMap<FacetConfiguration,List<Pair<String,String>>>();
		     for(Coverage coverage : report.facets.coverage) {
		         FacetConfiguration facetConfig = facetMapping.getFacetConfiguration(coverage.name);
		         
		         for(Pattern pattern : facetConfig.getPatterns()) {
		             ap.selectXPath(pattern.getPattern());
		             
		             int index = ap.evalXPath();

		             while (index != -1) {
		                 ValueNode node = nodesMap.get(index);
		                 
		                 processRawValue(node, facetConfig);
		             
		                 
		                 index = ap.evalXPath();
		             
		             }
		             
		         }
		         
		     }

			
			report.facets.values = nodesMap.values();
			
			double numOfCoveredByIns = report.facets.coverage.stream().filter(facet -> facet.coveredByInstance).count();
			report.facets.instanceCoverage = numOfCoveredByIns / report.facets.numOfFacets;

		}
		catch (Exception e) {
			_logger.error(e.getMessage());
			throw new Exception("Unable to obtain mapping for " + entity, e);
		};

	}
	
    private void processRawValue(ValueNode node, FacetConfiguration facetConfig) {
        Map<FacetConfiguration, List<String>> facetValuesMap = new HashMap<FacetConfiguration, List<String>>();

        boolean removeSourceValue = false;

        final List<String> normalizedValues = postNormalize(facetConfig.getName(), node.value);

        if (facetConfig.getConditionTargetSet() != null) {

            if (_postNormalizers.containsKey(facetConfig.getName()) && !(_postNormalizers.get(facetConfig.getName()) instanceof AbstractPostNormalizerWithVocabularyMap)) {
                for (String normalizedValue : normalizedValues) {
                    for (TargetFacet target : facetConfig.getConditionTargetSet().getTargetsFor(normalizedValue)) {
                        removeSourceValue |= target.getRemoveSourceValue();
                        
                        facetValuesMap.computeIfAbsent(facetConfig, k -> new ArrayList<String>()).add(target.getValue());

                    }
                }
            } 
            else 
            {
                for (TargetFacet target : facetConfig.getConditionTargetSet().getTargetsFor(node.value)) {
                    removeSourceValue |= target.getRemoveSourceValue();

                    facetValuesMap.computeIfAbsent(facetConfig, list -> new ArrayList<String>()).add(target.getValue());

                }
            }

        }

        if (!removeSourceValue) { // positive 'removeSourceValue' means skip adding value to origin facet
            for (String normalizedValue : normalizedValues) {
                
                facetValuesMap.computeIfAbsent(facetConfig, list -> new ArrayList<String>()).add(normalizedValue);
            }

        }
        
        
        node.facet = new ArrayList<FacetValueStruct>();
        for(Map.Entry<FacetConfiguration, List<String>> entry : facetValuesMap.entrySet()) {
            node.facet.add(createFacetValueStruct(facetConfig.getName(), node.value, entry.getValue(), false));
            
        }
        
    }
    
    private List<String> postNormalize(String facetName, String extractedValue) {
        List<String> resultList = new ArrayList<>();
        if (_postNormalizers.containsKey(facetName)) {
            AbstractPostNormalizer processor = _postNormalizers.get(facetName);
            resultList = processor.process(extractedValue, null);
        } else {
            resultList.add(extractedValue);
        }
        return resultList;
    }
	
	@Override
	public Score calculateScore(CMDInstanceReport report) {
		return new Score(report.facets.instanceCoverage, 1.0, "facet-mapping", msgs);
	}

	/*
	 * facet value normalisation is done here
	 */
	
	private FacetValueStruct createFacetValueStruct(String facetName, String value, List<String> normalizedValues, boolean isDerived){
		
		FacetValueStruct facetNode = new FacetValueStruct();
		facetNode.name = facetName;
		facetNode.isDerived = isDerived? true : null;
		facetNode.normalisedValue = null;
		

		
		String normalisedValue = normalizedValues.stream().collect(Collectors.joining(";"));
		if(normalisedValue != null && !normalisedValue.trim().isEmpty() && !normalisedValue.equals("--") && !value.equals(normalisedValue)){
			addMessage(Severity.INFO, "Normalised value for facet " + facetName + ": '" + value + "' into '" + normalisedValue + "'");
			facetNode.normalisedValue = normalisedValue;
		}
			
		return facetNode;	
		
	}

}
