package eu.clarin.cmdi.curation.api.subprocessor.instance;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport.ResourceType;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.PIDUtils;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * The type Resource proxy processor.
 */
@Component
@Slf4j
public class ResourceProxyProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   /**
    * Process.
    *
    * @param instance       the instance
    * @param instanceReport the instance report
    */
   @Override
   public void process(CMDInstance instance, CMDInstanceReport instanceReport) {
      
      if(instance.getCmdiData().isEmpty()) {
         
         log.debug("can't create CMDData object from file '{}'", instance.getPath());
         instanceReport.details
               .add(new Detail(Severity.FATAL, "file", "can't parse file '" + instance.getPath().getFileName() + "'"));
         instanceReport.isProcessable = false;
         
         return;
      }
      
      instanceReport.resProxyReport = new ResourceProxyReport();
      
      CMDIData<Map<String, List<ValueSet>>> data = instance.getCmdiData().get();
      


      addResourceType(data.getDataResources(), instanceReport);
      addResourceType(data.getLandingPageResources(), instanceReport);
      addResourceType(data.getMetadataResources(), instanceReport);
      addResourceType(data.getSearchPageResources(), instanceReport);
      addResourceType(data.getSearchResources(), instanceReport);
      
      if(instanceReport.resProxyReport.numOfResources == 0) {
         
         instanceReport.details.add(new Detail(Severity.FATAL, "resource proxy", "no resources in file '" + instance.getPath().getFileName() + "'"));
         instanceReport.isProcessable = false;
      }
      else {
         
         instanceReport.resProxyReport.percOfResourcesWithMime = ((double) instanceReport.resProxyReport.numOfResourcesWithMime/instanceReport.resProxyReport.numOfResources);
         instanceReport.resProxyReport.percOfResourcesWithReference = ((double) instanceReport.resProxyReport.numOfResourcesWithReference/instanceReport.resProxyReport.numOfResources);
      }
      
      instanceReport.resProxyReport.score = instanceReport.resProxyReport.percOfResourcesWithMime + instanceReport.resProxyReport.percOfResourcesWithReference;
      instanceReport.instanceScore+=instanceReport.resProxyReport.score;
   }

   private void addResourceType(List<Resource> resources, CMDInstanceReport instanceReport) {
      if (resources.isEmpty())
         return;
      
      instanceReport.resProxyReport.resourceTypes.add(new ResourceType(resources.get(0).getType(), resources.size()));

      resources.forEach(resource -> {
         if (resource.getResourceName() != null && !resource.getResourceName().isEmpty()) {
            
            if(!PIDUtils.isActionableLink(resource.getResourceName()) && !PIDUtils.isPid(resource.getResourceName())) {
               instanceReport.resProxyReport.invalidReferences.add(resource.getResourceName());
            }
            else {
               instanceReport.resProxyReport.numOfResourcesWithReference++;
               
               if (resource.getMimeType() != null && !resource.getMimeType().isEmpty()) {
                  instanceReport.resProxyReport.numOfResourcesWithMime++;
               }               
            }
         }
         instanceReport.resProxyReport.numOfResources++;
      });
   }
}
