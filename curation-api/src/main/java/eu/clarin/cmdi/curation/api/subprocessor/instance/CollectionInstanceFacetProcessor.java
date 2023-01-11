package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.util.HashSet;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.cache.FacetReportCache;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.Coverage;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

@Component
public class CollectionInstanceFacetProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   private FacetReportCache facetReportCache;

   @Override
   public void process(CMDInstance entity, CMDInstanceReport report) {

      InstanceFacetReport facetReport = new InstanceFacetReport();
      report.setFacetReport(facetReport);

      facetReportCache.getFacetReport(report.getHeaderReport().getProfileHeader()).getCoverage()
            .forEach(profileCoverage -> facetReport.addCoverage(profileCoverage.getName(), profileCoverage.isCoveredByProfile()));


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

      facetReport.getCoverages().stream().filter(Coverage::isCoveredByProfile)
            .forEach(coverage -> coverage.setCoveredByInstance(originFacetsWithValue.contains(coverage.getName())));

   }
}
