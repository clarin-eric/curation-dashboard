package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Concept;
import eu.clarin.cmdi.curation.report.FacetReport;
import eu.clarin.cmdi.curation.report.FacetReport.FacetValueStruct;
import eu.clarin.cmdi.curation.report.FacetReport.ValueNode;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dostojic
 *
 */
public class InstanceFacetProcessor extends CMDSubprocessor {

	private final static Logger logger = LoggerFactory.getLogger(InstanceFacetProcessor.class);

	int numOfFacetsCoveredByIns = 0;


	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
				
		Map<String, CMDINode> elements = new CRService().getParsedProfile(report.header).getElements();
		ParsedInstance parsedInstance = entity.getParsedInstance();
		FacetMapping facetMapping;
		
		try{
			facetMapping = FacetMappingCacheFactory.getInstance().getFacetMapping(report.header);
						
			report.facets = new FacetReportCreator().createFacetReport(facetMapping);			
						
			createValueNodes(parsedInstance, elements, report.facets);			
			joinFacetsToValues(facetMapping, report.facets);
			
			double numOfCoveredByIns = report.facets.coverage.stream().filter(facet -> facet.coveredByInstance).count();
			report.facets.instanceCoverage = numOfCoveredByIns / report.facets.numOfFacets;

		}catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception("Unable to obtain mapping for " + entity, e);
		};

	}
	
	@Override
	public Score calculateScore(CMDInstanceReport report) {
		return new Score(report.facets.instanceCoverage, 1.0, "facet-mapping", msgs);
	}
	
	private void createValueNodes(ParsedInstance parsedInstance, Map<String, CMDINode> parsedProfile, FacetReport facetReport){
		
		if(facetReport.values == null){
			facetReport.values = new ArrayList<>();
		}
		
		parsedInstance.getNodes().stream()
		.filter(node -> !node.getValue().trim().isEmpty())
		.forEach(instanceNode -> {
			ValueNode val = new ValueNode();
			val.xpath = instanceNode.getXpath();
			val.value = instanceNode.getValue();
			
			CMDINode node = parsedProfile.get(instanceNode.getXpath());
			if(node != null && node.concept != null)
				val.concept = new Concept(node.concept.uri, node.concept.prefLabel, node.concept.status);
			
			facetReport.values.add(val);
			
		});
		
	}

	private void joinFacetsToValues(FacetMapping facetMapping, FacetReport facetReport){
		facetReport.coverage.stream().filter(f -> f.coveredByProfile).map(f -> f.name).forEach(facetName -> {
			FacetConfiguration facetConfig = facetMapping.getFacetConfiguration(facetName);
			
			boolean matchedPattern = false;
			Collection<Pattern> patterns = facetConfig.getPatterns();
			for (Pattern pattern : patterns) {
				matchedPattern = matchPattern(facetConfig, pattern.getPattern(), facetReport);
				if (matchedPattern && !facetConfig.getAllowMultipleValues()) {
					break;
				}
			}

			// using fallback patterns if extraction failed
			if (matchedPattern == false) {
				for (Pattern pattern : facetConfig.getFallbackPatterns()) {
					matchedPattern = matchPattern(facetConfig, pattern.getPattern(), facetReport);
					if (matchedPattern && !facetConfig.getAllowMultipleValues()) {
						break;
					}
				}
			}
		});
	}
	
	private boolean matchPattern(FacetConfiguration facetConfig, String pattern, FacetReport facetReport){
		
		boolean matchedPattern = false;
		
		for(ValueNode valueNode: facetReport.values){
			//remove array indexes, i.e. [1]
			String xpath = valueNode.xpath.replaceAll("\\[[0-9]+\\]", "");
			if(pattern.contains("//")){//generic xpath: compare only the last part
				String[] tokens = pattern.split("//");
				if(!xpath.endsWith(tokens[tokens.length - 1]))
					continue;
			}else if(pattern.matches(".*\\[@.*=\\\".*\\\"\\].*")){
				/* pattern has condition
				 * check first the condition
				 */
				String[] tokens = pattern.split("=");
				String attrXpath = tokens[0].replace('[', '/');
				String attrVal = tokens[1].substring(1, tokens[1].indexOf('"', 1));
				ValueNode conditional = facetReport.values
						.stream()
						.filter(iNode -> iNode.xpath.equals(attrXpath))
						.findFirst()
						.orElse(null);
				
				//attribute doens't exist or its value doesn't match the one from condition
				if(conditional == null || !conditional.value.equals(attrVal))
					continue;
			}else{
				if(!xpath.equals(pattern))
					continue;
			}
			
			//should obtain val from the pattern/@xml:lang but here we dont check it
			//final String languageCode = extractLanguageCode(navigator);
			
			//normalisation is done in this method
			FacetValueStruct facetNode = createFacetValueStruct(facetConfig.getName(), valueNode.value, false);
			
			if(facetNode != null){
				facetReport.coverage.stream().filter(f -> f.name.equals(facetConfig.getName())).findFirst().ifPresent(f -> f.coveredByInstance = true);
				matchedPattern = true;			
				
				if(valueNode.facet == null){
					valueNode.facet = new ArrayList<>();
				}
				
				valueNode.facet.add(facetNode);				
				
				//create derived facets and assigned values
				facetConfig.getDerivedFacets().forEach(df -> {
					FacetValueStruct derivedFacetNode = createFacetValueStruct(df.getName(), facetNode.normalisedValue, true);				
					
					if(derivedFacetNode != null){
						valueNode.facet.add(derivedFacetNode);
						//don't count derived facets 
						//facetReport.coveredByInstance.add(df);
					}
				});				
			}
			
			if (!facetConfig.getAllowMultipleValues())
				break;
			
			
		}
		
		return matchedPattern;
	}	

	/*
	 * facet value normalisation is done here
	 */
	
	private FacetValueStruct createFacetValueStruct(String facetName, String value, boolean isDerived){
		
		FacetValueStruct facetNode = new FacetValueStruct();
		facetNode.name = facetName;
		facetNode.isDerived = isDerived? true : null;
		facetNode.normalisedValue = null;
		
		if(value == null || value.isEmpty())
			return facetNode;
		
		PostProcessor postProcessor = getPostProcessor(facetName);
		if(postProcessor == null)
			return facetNode;
		
		String normalisedValue = String.join("; ", postProcessor.process(value));
		if(normalisedValue != null && !normalisedValue.trim().isEmpty() && !normalisedValue.equals("--") && !value.equals(normalisedValue)){
			addMessage(Severity.INFO, "Normalised value for facet " + facetName + ": '" + value + "' into '" + normalisedValue + "'");
			facetNode.normalisedValue = normalisedValue;
		}else if(facetName.equals(FacetConstants.FIELD_LICENSE) && normalisedValue.equals("--")){
			//ignore values normalized to '--' for license facet
			facetNode = null;
			addMessage(Severity.INFO, "Ignored value for facet "+ facetName + ": '" + value + "'. This value will be removed from mapping");
		}
			
		return facetNode;	
		
		
	}
	
	private PostProcessor getPostProcessor(String facetName){
		switch (facetName) {
			//case FacetConstants.FIELD_ID: return new IdPostProcessor();
			case FacetConstants.FIELD_CONTINENT:
				return new ContinentNamePostProcessor();
			case FacetConstants.FIELD_COUNTRY:
				return new CountryNamePostProcessor();
			case FacetConstants.FIELD_LANGUAGE_CODE:
				return new LanguageCodePostProcessor();
			case FacetConstants.FIELD_LANGUAGE_NAME:
				return new LanguageNamePostProcessor();
			case FacetConstants.FIELD_AVAILABILITY:
				return new AvailabilityPostProcessor();
			case FacetConstants.FIELD_ORGANISATION:
				return new OrganisationPostProcessor();
			case FacetConstants.FIELD_TEMPORAL_COVERAGE:
				return new TemporalCoveragePostProcessor();
			case FacetConstants.FIELD_NATIONAL_PROJECT:
				return new NationalProjectPostProcessor();
			//case FacetConstants.FIELD_CLARIN_PROFILE: return new CMDIComponentProfileNamePostProcessor();
			case FacetConstants.FIELD_RESOURCE_CLASS:
				return new ResourceClassPostProcessor();
			case FacetConstants.FIELD_LICENSE:
				return new LicensePostProcessor();
			default:
				return null;
		}
	}
}
