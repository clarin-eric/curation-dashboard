package eu.clarin.cmdi.curation.api.subprocessor.instance;

import com.ximpleware.*;
import eu.clarin.cmdi.curation.api.cache.ProfileReportCache;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.Coverage;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.FacetValueStruct;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.ValueNode;
import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.xml.XPathValueService;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.pph.exception.PPHServiceNotAvailableException;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Instance facet processor.
 */
@Slf4j
@Component
public class InstanceFacetProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   private ApiConfig conf;
   @Autowired
   private CRService crService;
   /**
    * The Profile report cache.
    */
   @Autowired
   ProfileReportCache profileReportCache;
   /**
    * The Xpath value service.
    */
   @Autowired
   XPathValueService xpathValueService;

   /**
    * Process.
    *
    * @param instance       the instance
    * @param instanceReport the instance report
    */
   @Override
   public void process(CMDInstance instance, CMDInstanceReport instanceReport) throws MalFunctioningProcessorException {
      
      if(instance.getCmdiData().isEmpty()) {
         
         log.debug("can't create CMDData object from file '{}'", instance.getPath());
         instanceReport.details
               .add(new Detail(Severity.FATAL, "file", "can't parse file '" + instance.getPath().getFileName() + "'"));
         instanceReport.isProcessable = false;
         
         return;
      }

      instanceReport.facetReport = new InstanceFacetReport();

      profileReportCache
            .getProfileReport(new CMDProfile(instanceReport.profileHeaderReport.getSchemaLocation(),
                  instanceReport.profileHeaderReport.getCmdiVersion())).facetReport.coverages
            .forEach(profileCoverage -> instanceReport.facetReport.coverages
                  .add(new Coverage(profileCoverage.name, profileCoverage.coveredByProfile)));

      instanceReport.facetReport.numOfFacets = instanceReport.facetReport.coverages.size();

      Map<String, List<ValueSet>> facetValuesMap = instance.getCmdiData().get().getDocument();
      


      // the key of the facetValuesMap is the target facet name
      instanceReport.facetReport.coverages.forEach(coverage -> {
         if (coverage.coveredByInstance = facetValuesMap.containsKey(coverage.name)) { // initialization and test!
            instanceReport.facetReport.numOfFacetsCoveredByInstance++;
         }
      });

      instanceReport.facetReport.percCoveragedByInstance = (double) instanceReport.facetReport.numOfFacetsCoveredByInstance
            / instanceReport.facetReport.numOfFacets;
      instanceReport.facetReport.score = instanceReport.facetReport.percCoveragedByInstance;
      instanceReport.instanceScore += instanceReport.facetReport.score;

      // in case of a single instance analysis we want to know for each node with a
      // value to which facet it is mapped
      if ("instance".equals(conf.getMode())) {

         try {
            Map<String, CMDINode> cmdiNodeMap = crService
                  .getParsedProfile(instanceReport.profileHeaderReport.getProfileHeader()).getXpathElementNode();

            final Map<Integer, List<ValueSet>> indexValueSetMap = facetValuesMap.values() // a List of ValueSet
                  .stream().flatMap(List::stream).collect(Collectors.groupingBy(ValueSet::getVtdIndex));

            VTDGen vtdGen = new VTDGen();
            vtdGen.setDoc(Files.readAllBytes(instance.getPath()));
            vtdGen.parse(false);

            VTDNav nav = vtdGen.getNav();
            AutoPilot ap = new AutoPilot(nav);

            // attention: we assume that the the xpath in the stream are in document order
            // if this is not the case a call of ap.resetXPath() is mandatory
            this.xpathValueService.getXpathValueMap(instance.getPath()).entrySet()
               .stream().filter(node -> StringUtils.isNotBlank(node.getValue()))
                  .forEach(node -> {

                     ValueNode valueNode = new ValueNode(node.getKey(), node.getValue());

                     CMDINode cmdiNode;

                     if ((cmdiNode = cmdiNodeMap.get(node.getKey().replaceAll("\\[\\d\\]", ""))) != null && cmdiNode.concept != null) {

                        valueNode.concept = new ConceptReport.Concept(cmdiNode.concept);
                     }

                     try {
                        ap.selectXPath(node.getKey().replaceAll("\\w+:", ""));

                        List<ValueSet> valueSetList;
                        
                        int vtdIndex = ap.evalXPath(); 

                        if (vtdIndex != -1 && (valueSetList = indexValueSetMap.get(vtdIndex)) != null) {
                           
                           valueSetList.forEach(valueSet -> {
                              valueNode.facets.add(
                                    new FacetValueStruct(
                                       valueSet.getTargetFacetName(),
                                       valueSet.isDerived(), 
                                       valueSet.isResultOfValueMapping(),
                                       valueSet.getValueLanguagePair().getLeft()
                                    )
                                 );                              
                              });
                        }
                     }
                     catch (XPathParseException e) {

                        log.error("can't parse xpath '{}'", node.getKey());
                     }
                     catch (XPathEvalException e) {

                        log.error("can't evalute xpath '{}'", node.getKey());
                     }
                     
                     catch (NavException e) {

                        log.error("", e);
                     }

                     instanceReport.facetReport.valueNodes.add(valueNode);
                  });

         }
         catch (NoProfileCacheEntryException e1) {
            
            log.error("no parsable profile '{}'", instanceReport.instanceHeaderReport.schemaLocation);
            instanceReport.details.add(new Detail(Severity.FATAL, "facets", "can't parse profile '" + instanceReport.instanceHeaderReport.schemaLocation + "'"));
            instanceReport.isProcessable = false;

         }
         catch (IOException e1) {

            log.error("can't read file '{}'", instance.getPath());
            instanceReport.details.add(new Detail(Severity.FATAL, "file", "can't read file '" + instance.getPath().getFileName() + "'"));
            instanceReport.isProcessable = false;
         }
         catch (ParseException e1) {
            
            log.error("can't parse file '{}'", instance.getPath());
            instanceReport.details.add(new Detail(Severity.FATAL, "file", "can't parse file '" + instance.getPath().getFileName() + "'"));
            instanceReport.isProcessable = false;
         }
         catch (CRServiceStorageException | PPHServiceNotAvailableException | CCRServiceNotAvailableException e) {
             throw new MalFunctioningProcessorException(e);
         }
      }
   }
}
