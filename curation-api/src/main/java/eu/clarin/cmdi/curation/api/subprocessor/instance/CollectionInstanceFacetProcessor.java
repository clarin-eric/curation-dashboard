package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.cache.ProfileReportCache;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.entity.CMDProfile;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.Coverage;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

@Component
public class CollectionInstanceFacetProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   ProfileReportCache profileReportCache;

   @Override
   public void process(CMDInstance entity, CMDInstanceReport report) {

      report.facetReport = new InstanceFacetReport();

      profileReportCache.getProfileReport(new CMDProfile(report.profileHeaderReport.getSchemaLocation(), report.profileHeaderReport.getCmdiVersion())).facetReport.coverages
      .forEach(profileCoverage -> report.facetReport.coverages.add(new Coverage(profileCoverage.name, profileCoverage.coveredByProfile)));
      
      report.facetReport.numOfFacets = report.facetReport.coverages.size();

      Map<String, List<ValueSet>> facetValuesMap = entity.getCmdiData().getDocument();

      //the key of the facetValuesMap is the target facet name
      report.facetReport.coverages.stream()
            .forEach(coverage -> {
               if(coverage.coveredByInstance = facetValuesMap.keySet().contains(coverage.name)) { //initialization and test!
                  report.facetReport.numOfFacetsCoveredByInstance++;
               };
            });
      
      report.facetReport.percCoveragedByInstance = (double) report.facetReport.numOfFacetsCoveredByInstance/report.facetReport.numOfFacets;
      report.facetReport.score=report.facetReport.percCoveragedByInstance;
      report.instanceScore+=report.facetReport.score;
      
   }
}
