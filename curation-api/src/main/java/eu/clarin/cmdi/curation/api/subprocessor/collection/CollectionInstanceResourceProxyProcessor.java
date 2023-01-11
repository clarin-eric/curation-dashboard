package eu.clarin.cmdi.curation.api.subprocessor.collection;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

@Component
public class CollectionInstanceResourceProxyProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {
   


   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) {
      
      ResourceProxyReport resProxyReport = new ResourceProxyReport();
      report.setResProxyReport(resProxyReport);
      
      CMDIData<Map<String, List<ValueSet>>> data = instance.getCmdiData();

      Stream<Resource> resourceStream = Stream.of(data.getDataResources().stream(),
            data.getLandingPageResources().stream(), data.getMetadataResources().stream(),
            data.getSearchPageResources().stream(), data.getSearchResources().stream()).flatMap(s -> s);


      resourceStream.forEach(resource -> {

         resProxyReport.incrementNumOfResProxies();

         if (resource.getMimeType() != null) {
            resProxyReport.incrementNumOfResourcesWithMime();
         }
         if (resource.getResourceName() != null && !resource.getResourceName().isEmpty())
            resProxyReport.incrementNumOfResProxiesWithReferences();

      });

      Stream.of(data.getDataResources(), data.getLandingPageResources(), data.getMetadataResources(),
            data.getSearchPageResources(), data.getSearchResources())
               .filter(list -> list.size() >0) 
               .forEach(list -> resProxyReport.addResourceType(list.get(0).getType(), list.size()));
   }
}
