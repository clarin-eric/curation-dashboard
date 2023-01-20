package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ximpleware.*;

import eu.clarin.cmdi.curation.api.cache.ProfileReportCache;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.api.instance_parser.ParsedInstance.InstanceNode;
import eu.clarin.cmdi.curation.api.report.Issue;
import eu.clarin.cmdi.curation.api.report.Issue.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.Coverage;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.FacetValueStruct;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.ValueNode;
import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport.Concept;
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
   ProfileReportCache profileReportCache;

   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) {
      
      report.facetReport = new InstanceFacetReport();
      
      profileReportCache.getProfileReport(new CMDProfile(report.headerReport.getSchemaLocation(), report.headerReport.getCmdiVersion())).facetReport.coverages
         .forEach(profileCoverage -> report.facetReport.coverages.add(new Coverage(profileCoverage.name, profileCoverage.coveredByProfile)));
      
      

      // parse instance
      CMDXPathService xmlService;
      try {
         xmlService = new CMDXPathService(instance.getPath());
      }
      catch (ParseException e) {

         log.error("can't parse file '{}' for instance facet processing", instance.getPath());

         report.issues.add(new Issue(Severity.FATAL, "facet",
               "can't parse file '" + instance.getPath().getFileName() + "' for instance facet processing"));
         report.isValidReport=false;

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

      report.facetReport.valueNodes.addAll(nodesMap.values());
      
      report.facetReport.percCoveragedByInstance = report.facetReport.numOfFacetsCoveredByInstance/report.facetReport.numOfFacets;
      report.facetReport.score=report.facetReport.percCoveragedByInstance;
      report.instanceScore+=report.facetReport.score;

   }

   private Map<Integer, ValueNode> getValueNodesMap(CMDInstance instance, CMDInstanceReport report,
         VTDNav nav) {
      Map<Integer, ValueNode> nodesMap = new LinkedHashMap<Integer, ValueNode>();

      Map<String, CMDINode> elements;
      try {
         elements = crService.getParsedProfile(report.headerReport.getProfileHeader()).getElements();

         ParsedInstance parsedInstance = instance.getParsedInstance();

         AutoPilot ap = new AutoPilot(nav);

         // create value nodes
         for (InstanceNode instanceNode : parsedInstance.getNodes()) {
            if (!instanceNode.getValue().isEmpty()) {
               ValueNode val = new ValueNode(instanceNode.getXpath(), instanceNode.getValue());
               report.facetReport.valueNodes.add(val);

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
                  report.issues.add(new Issue(Severity.FATAL, "facet","can't navigate in document"));
               }
               ap.resetXPath();
            }
         }
      }
      catch (NoProfileCacheEntryException e) {

         log.debug("no ProfileCacheEntry for profile id '{}'", report.headerReport.getId());

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

         Coverage coverage = report.facetReport.coverages.stream().filter(c -> c.name.equals(facetName)).findFirst()
               .orElse(null);

         if (coverage != null) {
            // lambda expression to ignore values set by cross facet mapping
            if(coverage.coveredByInstance = originFacetsWithValue.contains(facetName)) {
               report.facetReport.numOfFacetsCoveredByInstance++;
            };
         }

         // in the next step the value(s) have to be mapped to the right node

         Map<Integer, List<ValueSet>> facetMap = values.stream().collect(Collectors.groupingBy(ValueSet::getVtdIndex)); // groups
                                                                                                                        // vtdIndex

         for (Map.Entry<Integer, List<ValueSet>> entry : facetMap.entrySet()) {

            if (nodesMap.containsKey(entry.getKey())) { // there is a node where the xpath has the same vtdIndex
               ValueNode node = nodesMap.get(entry.getKey());

               String normalizedValue = entry.getValue().stream().map(valueSet -> valueSet.getValueLanguagePair().getLeft())
                     .collect(Collectors.joining("; "));
               
               if(node.value.equals(normalizedValue)) {
                  normalizedValue = null;
               }
               if (normalizedValue != null && !normalizedValue.trim().isEmpty() && !normalizedValue.equals("--")) {
                  report.issues.add(new Issue(Severity.INFO, "facet",
                        "Normalised value for facet " + facetName + ": '" + node.value + "' into '" + normalizedValue + "'"));
               }

               node.facets.add(new FacetValueStruct(
                     facetName, 

                     // entry.getValue().get(0).isDerived() //assumes that a facet isn't defined as
                     // origin and derived at the same time
                     entry.getValue().stream().anyMatch(ValueSet::isDerived), // assumes that a facet isn't
                                                                              // defined as origin and derived at
                                                                              // the same time
                     entry.getValue().stream().anyMatch(ValueSet::isResultOfValueMapping), // assumes that a facet                                                                       // origin and derived
                                                                                          // at the same time
                     normalizedValue
               ));
               
               
            }
         }
         coverage.coveredByInstance = true; // one or more values for the specific facet
      }
   }
}
