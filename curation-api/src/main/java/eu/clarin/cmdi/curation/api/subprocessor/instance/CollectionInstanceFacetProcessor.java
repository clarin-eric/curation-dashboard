package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.util.HashSet;

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

      profileReportCache.getProfileReport(new CMDProfile(report.headerReport.getSchemaLocation(), report.headerReport.getCmdiVersion())).facetReport.coverages
      .forEach(profileCoverage -> report.facetReport.coverages.add(new Coverage(profileCoverage.name, profileCoverage.coveredByProfile)));


      Map<String, List<ValueSet>> facetValuesMap = entity.getCmdiData().getDocument();

      /*
       * We need to know if a value is mapped to the origin facet. The facetValuesMap
       * has the name of the target facet key. When using cross facet mapping the
       * target facet is not the same as the origin facet. Therefore we extract the
       * origin facet from each the ValueSet and we can assume that for each origin
       * facet a value was mapped to this origin facet
       */
      HashSet<String> originFacetsWithValue = new HashSet<String>();
      facetValuesMap.values().forEach(
            list -> list.forEach(valueSet -> originFacetsWithValue.add(valueSet.getOriginFacetConfig().getName())));

      report.facetReport.coverages.stream().filter(coverage -> coverage.coveredByProfile)
            .forEach(coverage -> {
               if(coverage.coveredByInstance = originFacetsWithValue.contains(coverage.name)) {
                  report.facetReport.numOfFacetsCoveredByInstance++;
               };
            });
      report.facetReport.percCoveragedByInstance = report.facetReport.numOfFacetsCoveredByInstance/report.facetReport.numOfFacets;
      report.facetReport.score=report.facetReport.percCoveragedByInstance;
      report.instanceScore+=report.facetReport.score;
   }
}
