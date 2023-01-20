package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport.ResourceType;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.PIDUtils;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

@Component
public class ResourceProxyProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {  

   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) {
      
      report.resProxyReport = new ResourceProxyReport();
      
      CMDIData<Map<String, List<ValueSet>>> data = instance.getCmdiData();

      addResourceType(data.getDataResources(), report);
      addResourceType(data.getLandingPageResources(), report);
      addResourceType(data.getMetadataResources(), report);
      addResourceType(data.getSearchPageResources(), report);
      addResourceType(data.getSearchResources(), report);
      
      if(report.resProxyReport.numOfResProxies > 0) {
         report.resProxyReport.percOfResourcesWithMime = ((double) report.resProxyReport.numOfResourcesWithMime/report.resProxyReport.numOfResProxies);
         report.resProxyReport.percOfResProxiesWithReference = ((double) report.resProxyReport.numOfResProxiesWithReference/report.resProxyReport.numOfResProxies);
      }
      
      report.resProxyReport.score = report.resProxyReport.percOfResourcesWithMime + report.resProxyReport.percOfResProxiesWithReference;
      report.instanceScore+=report.resProxyReport.score;
   }

   private void addResourceType(List<Resource> resources, CMDInstanceReport report) {
      if (resources.isEmpty())
         return;
      
      report.resProxyReport.resourceTypes.add(new ResourceType(resources.get(0).getType(), resources.size()));

      resources.forEach(resource -> {
         if (resource.getResourceName() != null && !resource.getResourceName().isEmpty()) {
            report.resProxyReport.numOfResProxiesWithReference++;
            
            if(PIDUtils.isPid(resource.getResourceName())) {
               report.resProxyReport.invalidReferences.add(resource.getResourceName());
            }
         }
         if (resource.getMimeType() != null && !resource.getMimeType().isEmpty()) {
            report.resProxyReport.numOfResourcesWithMime++;
         }

         report.resProxyReport.numOfResProxies++;
      });
   }
}
