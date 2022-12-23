package eu.clarin.cmdi.curation.api.subprocessor.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport.ResProxyReport;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport.ResourceType;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

@Component
public class CollectionInstanceResourceProxyProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) {
      
      Score score = new Score("cmd-res-proxy", 2.0);
      
      CMDIData<Map<String, List<ValueSet>>> data = instance.getCmdiData();

      Stream<Resource> resourceStream = Stream.of(data.getDataResources().stream(),
            data.getLandingPageResources().stream(), data.getMetadataResources().stream(),
            data.getSearchPageResources().stream(), data.getSearchResources().stream()).flatMap(s -> s);

      report.resProxyReport = new ResProxyReport();

      report.resProxyReport.numOfResProxies = 0;
      report.resProxyReport.numOfResourcesWithMime = 0;
      report.resProxyReport.numOfResProxiesWithReferences = 0;
      report.resProxyReport.resourceType = new ArrayList<ResourceType>();

      resourceStream.forEach(resource -> {

         report.resProxyReport.numOfResProxies++;

         if (resource.getMimeType() != null)
            report.resProxyReport.numOfResourcesWithMime++;
         if (resource.getResourceName() != null && !resource.getResourceName().isEmpty())
            report.resProxyReport.numOfResProxiesWithReferences++;

      });

      Stream.of(data.getDataResources(), data.getLandingPageResources(), data.getMetadataResources(),
            data.getSearchPageResources(), data.getSearchResources()).forEach(list -> {
               if (list.size() > 0) {

                  ResourceType resourceType = new ResourceType();
                  resourceType.type = list.get(0).getType();
                  resourceType.count = list.size();

                  report.resProxyReport.resourceType.add(resourceType);
               }
            });
      
      score.setScore(report.resProxyReport.percOfResourcesWithMime + report.resProxyReport.percOfResProxiesWithReferences);
      
      report.addSegmentScore(score);
   }
}
