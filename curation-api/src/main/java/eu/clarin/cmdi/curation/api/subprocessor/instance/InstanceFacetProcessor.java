package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ximpleware.*;

import eu.clarin.cmdi.curation.api.cache.FacetReportCache;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.api.instance_parser.ParsedInstance.InstanceNode;
import eu.clarin.cmdi.curation.api.report.Scoring;
import eu.clarin.cmdi.curation.api.report.Scoring.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.section.InstanceFacetReport;
import eu.clarin.cmdi.curation.api.report.instance.section.InstanceFacetReport.Coverage;
import eu.clarin.cmdi.curation.api.report.instance.section.InstanceFacetReport.FacetValueStruct;
import eu.clarin.cmdi.curation.api.report.instance.section.InstanceFacetReport.ValueNode;
import eu.clarin.cmdi.curation.api.report.profile.section.ConceptReport.Concept;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.xml.CMDXPathService;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;

import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InstanceFacetProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   private ApiConfig conf;
   @Autowired
   private CRService crService;
   @Autowired
   FacetReportCache facetReportCache;

   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) {
      
      final InstanceFacetReport facetReport = new InstanceFacetReport();
      report.setFacetReport(facetReport);
      
      facetReportCache.getFacetReport(report.getHeaderReport().getProfileHeader()).getCoverage().forEach(profileCoverage -> {
         
         Coverage coverage = new Coverage();
         coverage.setName(profileCoverage.getName());
         coverage.setCoveredByProfile(profileCoverage.isCoveredByProfile());
         facetReport.getCoverage().add(coverage);
         
      });
      
      

      // parse instance
      CMDXPathService xmlService;
      try {
         xmlService = new CMDXPathService(instance.getPath());
      }
      catch (ParseException e) {

         log.error("can't parse file '{}' for instance facet processing", instance.getPath());

         report.getFacetReport().getScore().addMessage(Severity.FATAL,
               "can't parse file '" + instance.getPath().getFileName() + "' for instance facet processing");

         return;

      }
      catch (IOException e) {

         log.error("can't read file '{}' for instance facet processing", instance.getPath());
         throw new RuntimeException(e);

      }

      VTDNav nav = xmlService.getNavigator();

      Map<Integer, ValueNode> nodesMap;

      nodesMap = getValueNodesMap(instance, report, nav);

      log.trace("nodes map: \n{}", nodesMap);

      facetsToNodes(instance, report, nodesMap, nav);

      facetReport.getValues().addAll(nodesMap.values());

   }

   private Map<Integer, ValueNode> getValueNodesMap(CMDInstance instance, CMDInstanceReport report,
         VTDNav nav) {
      Map<Integer, ValueNode> nodesMap = new LinkedHashMap<Integer, ValueNode>();

      Map<String, CMDINode> elements;
      try {
         elements = crService.getParsedProfile(report.getHeaderReport().getProfileHeader()).getElements();

         ParsedInstance parsedInstance = instance.getParsedInstance();

         AutoPilot ap = new AutoPilot(nav);

         // create value nodes
         for (InstanceNode instanceNode : parsedInstance.getNodes()) {
            if (!instanceNode.getValue().isEmpty()) {
               ValueNode val = new ValueNode();
               val.xpath = instanceNode.getXpath();
               val.value = instanceNode.getValue();

               CMDINode node = elements.get(instanceNode.getXpath().replaceAll("\\[\\d\\]", ""));
               if (node != null && node.concept != null)
                  val.concept = new Concept(node.concept);

               // determines the index for each xpath
               String xpath = instanceNode.getXpath().replaceAll("\\w+:", "");
               try {
                  ap.selectXPath(xpath);
               }
               catch (XPathParseException e) {

                  log.debug("can't parse xpath '{}'", xpath);
                  throw new RuntimeException(e);
               }
               try {
                  nodesMap.put(ap.evalXPath(), val);
               }
               catch (XPathEvalException e) {

                  log.debug("can't evaluate xpath '{}'", xpath);
                  throw new RuntimeException(e);
               }
               catch (NavException e) {

                  log.debug("can't navigate in document");
                  report.getFacetReport().getScore().addMessage(Severity.FATAL, "can't navigate in document");
               }
               ap.resetXPath();
            }
         }
      }
      catch (NoProfileCacheEntryException e) {

         log.debug("no ProfileCacheEntry for profile id '{}'", report.getHeaderReport().getId());

      }

      return nodesMap;
   }

   private void facetsToNodes(CMDInstance instance, CMDInstanceReport report,
         Map<Integer, ValueNode> nodesMap, VTDNav nav) {

      Map<String, List<ValueSet>> facetValuesMap = instance.getCmdiData().getDocument();

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


      for (String facetName : conf.getFacets()) {
         List<ValueSet> values = facetValuesMap.get(facetName);

         if (values == null) // no values from instance for this facet
            continue;

         Coverage coverage = report.getFacetReport().getCoverage().stream().filter(c -> c.getName().equals(facetName)).findFirst()
               .orElse(null);

         if (coverage != null) {
            // lambda expression to ignore values set by cross facet mapping
            coverage.setCoveredByInstance(originFacetsWithValue.contains(facetName));
         }

         // in the next step the value(s) have to be mapped to the right node

         Map<Integer, List<ValueSet>> facetMap = values.stream().collect(Collectors.groupingBy(ValueSet::getVtdIndex)); // groups
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
                     entry.getValue().stream().anyMatch(ValueSet::isResultOfValueMapping), // assumes that a facet                                                                       // origin and derived
                                                                                          // at the same time
                     report.getFacetReport().getScore()
               ));
            }

         }
         coverage.setCoveredByInstance(true); // one or more values for the specific facet

      }
   }

   /*
    *
    */

   private FacetValueStruct createFacetValueStruct(String facetName, String value, String normalizedValue,
         boolean isDerived, boolean usesValueMapping, Scoring score) {

      FacetValueStruct facetNode = new FacetValueStruct();
      facetNode.name = facetName;
      facetNode.isDerived = isDerived ? true : null;
      facetNode.usesValueMapping = usesValueMapping ? true : null;
      facetNode.normalisedValue = value.equals(normalizedValue) ? null : normalizedValue;

      if (normalizedValue != null && !normalizedValue.trim().isEmpty() && !normalizedValue.equals("--")
            && !value.equals(normalizedValue)) {
         score.addMessage(Severity.INFO,
               "Normalised value for facet " + facetName + ": '" + value + "' into '" + normalizedValue + "'");
         facetNode.normalisedValue = normalizedValue;
      }

      return facetNode;

   }
}
