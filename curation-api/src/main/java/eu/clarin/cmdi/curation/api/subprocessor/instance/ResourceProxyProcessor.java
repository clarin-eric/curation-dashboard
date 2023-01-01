package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.section.ResProxyReport.ResourceType;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

@Component
public class ResourceProxyProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) {
      
      CMDIData<Map<String, List<ValueSet>>> data = instance.getCmdiData();

      addResourceType(data.getDataResources(), report);
      addResourceType(data.getLandingPageResources(), report);
      addResourceType(data.getMetadataResources(), report);
      addResourceType(data.getSearchPageResources(), report);
      addResourceType(data.getSearchResources(), report);

   }

   private void addResourceType(List<Resource> resources, CMDInstanceReport report) {
      if (resources.isEmpty())
         return;

      ResourceType resourceType = new ResourceType();
      resourceType.type = resources.get(0).getType();
      resourceType.count = resources.size();

      for (Resource resource : resources) {
         if (resource.getResourceName() != null && !resource.getResourceName().isEmpty())
            report.getResProxyReport().incrementNumOfResProxiesWithReferences();
         if (resource.getMimeType() != null && !resource.getMimeType().isEmpty())
            report.getResProxyReport().incrementNumOfResourcesWithMime();

         report.getResProxyReport().incrementNumOfResProxies();
      }
      report.getResProxyReport().getResourceType().add(resourceType);
   }
}
