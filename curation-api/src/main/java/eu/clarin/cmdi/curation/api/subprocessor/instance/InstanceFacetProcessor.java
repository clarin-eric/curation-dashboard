package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ximpleware.*;

import eu.clarin.cmdi.curation.api.configuration.CurationConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.api.instance_parser.ParsedInstance.InstanceNode;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport.FacetReport.Coverage;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport.FacetReport.FacetValueStruct;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport.FacetReport.ValueNode;
import eu.clarin.cmdi.curation.api.report.Concept;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.report.Severity;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.subprocessor.ext.FacetReportCreator;
import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.curation.api.xml.CMDXPathService;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;

import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InstanceFacetProcessor extends AbstractSubprocessor {

   @Autowired
   private CurationConfig conf;
   @Autowired
   private CRService crService;
   @Autowired
   private FacetsMappingCacheFactory fac;
   @Autowired
   ApplicationContext ctx;

   @Override
   public synchronized void process(CMDInstance entity, CMDInstanceReport report) throws SubprocessorException {

      // parse instance
      CMDXPathService xmlService;
      try {
         xmlService = new CMDXPathService(entity.getPath());
      }
      catch (ParseException e) {

         log.error("can't parse file '{}' for instance facet processing", entity.getPath());
         throw new SubprocessorException(e);

      }
      catch (IOException e) {

         log.error("can't read file '{}' for instance facet processing", entity.getPath());
         throw new RuntimeException(e);

      }

      VTDNav nav = xmlService.getNavigator();

      Map<Integer, ValueNode> nodesMap;

      nodesMap = getValueNodesMap(entity, report, nav);


      log.trace("nodes map: \n{}", nodesMap);

      facetsToNodes(entity, report, nodesMap, nav);

      report.facets.values = nodesMap.values();

      double numOfCoveredByIns = report.facets.coverage.stream().filter(facet -> facet.coveredByInstance).count();
      report.facets.instanceCoverage = numOfCoveredByIns / report.facets.numOfFacets;

   }

   private Map<Integer, ValueNode> getValueNodesMap(CMDInstance entity, CMDInstanceReport report, VTDNav nav) throws SubprocessorException {
      Map<Integer, ValueNode> nodesMap = new LinkedHashMap<Integer, ValueNode>();

      Map<String, CMDINode> elements;
      try {
         elements = crService.getParsedProfile(report.header).getElements();
      }
      catch (NoProfileCacheEntryException e) {
         
         log.debug("no ProfileCacheEntry for profile id '{}'", report.header.getId());
         
         throw new SubprocessorException(e);
      }
      ParsedInstance parsedInstance = entity.getParsedInstance();

      AutoPilot ap = new AutoPilot(nav);

      // create value nodes
      for (InstanceNode instanceNode : parsedInstance.getNodes()) {
         if (!instanceNode.getValue().isEmpty()) {
            ValueNode val = new ValueNode();
            val.xpath = instanceNode.getXpath();
            val.value = instanceNode.getValue();

            CMDINode node = elements.get(instanceNode.getXpath().replaceAll("\\[\\d\\]", ""));
            if (node != null && node.concept != null)
               val.concept = new Concept(node.concept.uri, node.concept.prefLabel, node.concept.status);

            // determines the index for each xpath
            String xpath = instanceNode.getXpath().replaceAll("\\w+:", "");
            try {
               ap.selectXPath(xpath);
            }
            catch (XPathParseException e) {
               
               log.debug("can't parse xpath '{}'", xpath);
               throw new SubprocessorException(e);
            }
            try {
               nodesMap.put(ap.evalXPath(), val);
            }
            catch (XPathEvalException e) {
               
               log.debug("can't evaluate xpath '{}'", xpath);
               throw new SubprocessorException(e);
            }
            catch (NavException e) {
               
               log.debug("can't navigate in document");
               throw new SubprocessorException(e);
            }
            ap.resetXPath();
         }
      }

      return nodesMap;
   }

   private void facetsToNodes(CMDInstance entity, CMDInstanceReport report, Map<Integer, ValueNode> nodesMap,
         VTDNav nav) throws SubprocessorException {

      FacetsMapping facetMapping = fac.getFacetsMapping(report.header);

      Map<String, List<ValueSet>> facetValuesMap = entity.getCmdiData().getDocument();

      /*
       * We need to know if a value is mapped to the origin facet. The facetValuesMap
       * has the name of the target facet key. When using cross facet mapping the
       * target facet is not the same as the origin facet. Therefore we extract the
       * origin facet from each the ValueSet and we can assume that for each origin
       * facet a vlaue was mapped to this origin facet
       */
      HashSet<String> originFacetsWithValue = new HashSet<String>();
      facetValuesMap.values().forEach(
            list -> list.forEach(valueSet -> originFacetsWithValue.add(valueSet.getOriginFacetConfig().getName())));

      report.facets = ctx.getBean(FacetReportCreator.class).createFacetReport(report.header, facetMapping);

      int numOfCoveredByIns = 0;

      for (String facetName : conf.getFacets()) {
         List<ValueSet> values = facetValuesMap.get(facetName);

         if (values == null) // no values from instance for this facet
            continue;

         Coverage coverage = report.facets.coverage.stream().filter(c -> c.name.equals(facetName)).findFirst()
               .orElse(null);

         if (coverage != null) {
            // lambda expression to ignore values set by cross facet mapping
            coverage.coveredByInstance = originFacetsWithValue.contains(facetName);

            if (coverage.coveredByInstance)
               numOfCoveredByIns++;
         }

         // in the next step the value(s) have to be mapped to the right node

         Map<Integer, List<ValueSet>> facetMap = values.stream().collect(Collectors.groupingBy(ValueSet::getVtdIndex)); // groups
                                                                                                                        // valueSets
                                                                                                                        // by
                                                                                                                        // vtdIndex

         for (Map.Entry<Integer, List<ValueSet>> entry : facetMap.entrySet()) {

            if (nodesMap.containsKey(entry.getKey())) { // there is a node where the xpath has the same vtdIndex
               ValueNode node = nodesMap.get(entry.getKey());

               // initializing node.facet if not done already:
               if (node.facet == null)
                  node.facet = new ArrayList<FacetValueStruct>();

               node.facet.add(createFacetValueStruct(facetName, node.value,
                     entry.getValue().stream().map(valueSet -> valueSet.getValueLanguagePair().getLeft())
                           .collect(Collectors.joining("; ")),
                     // entry.getValue().get(0).isDerived() //assumes that a facet isn't defined as
                     // origin and derived at the same time
                     entry.getValue().stream().anyMatch(ValueSet::isDerived), // assumes that a facet isn't
                                                                              // defined as origin and derived at
                                                                              // the same time
                     entry.getValue().stream().anyMatch(ValueSet::isResultOfValueMapping) // assumes that a facet
                                                                                          // isn't defined as
                                                                                          // origin and derived
                                                                                          // at the same time
               ));
            }

         }
         coverage.coveredByInstance = true; // one or more values for the specific facet
         numOfCoveredByIns++;

      }

      report.facets.instanceCoverage = report.facets.numOfFacets == 0 ? 0.0
            : (numOfCoveredByIns / (double) report.facets.numOfFacets); // cast to double to get a double as result

   }

   @Override
   public synchronized Score calculateScore(CMDInstanceReport report) {
      return new Score(report.facets.instanceCoverage, 1.0, "facet-mapping", this.getMessages());
   }

   /*
    *
    */

   private FacetValueStruct createFacetValueStruct(String facetName, String value, String normalizedValue,
         boolean isDerived, boolean usesValueMapping) {

      FacetValueStruct facetNode = new FacetValueStruct();
      facetNode.name = facetName;
      facetNode.isDerived = isDerived ? true : null;
      facetNode.usesValueMapping = usesValueMapping ? true : null;
      facetNode.normalisedValue = value.equals(normalizedValue) ? null : normalizedValue;

      if (normalizedValue != null && !normalizedValue.trim().isEmpty() && !normalizedValue.equals("--")
            && !value.equals(normalizedValue)) {
         addMessage(Severity.INFO,
               "Normalised value for facet " + facetName + ": '" + value + "' into '" + normalizedValue + "'");
         facetNode.normalisedValue = normalizedValue;
      }

      return facetNode;

   }
}
