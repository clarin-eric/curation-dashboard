package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.util.HashSet;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.CMDProfileReport.FacetReport.Coverage;
import eu.clarin.cmdi.curation.api.report.Score;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.subprocessor.ext.FacetReportCreator;
import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

@Component
public class CollectionInstanceFacetProcessor extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {
   
   @Autowired
   private FacetsMappingCacheFactory fac;
   @Autowired
   private FacetReportCreator facetReportCreator;

   @Override
   public void process(CMDInstance entity, CMDInstanceReport report) {
      
      Score score = new Score("facet-mapping", 1.0);

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

      entity.setParsedInstance(null);

      FacetsMapping facetMapping;
      try {
         facetMapping = fac.getFacetsMapping(report.header);

         report.facets = facetReportCreator.createFacetReport(score, report.header, facetMapping);

         int numOfCoveredByIns = 0;

         for (Coverage coverage : report.facets.coverage) {
            if (!coverage.coveredByProfile)
               continue; // we have to discuss if this should still be the case

            coverage.coveredByInstance = originFacetsWithValue.contains(coverage.name);
            if (coverage.coveredByInstance)
               numOfCoveredByIns++;
         }

         report.facets.instanceCoverage = report.facets.numOfFacets == 0 ? 0.0
               : (numOfCoveredByIns / (double) report.facets.numOfFacets); // cast to double to get a double as result

      }
      finally {
         entity.setCmdiData(null);
         entity.setParsedInstance(null);
      }
      
      score.setScore(report.facets.instanceCoverage);
      
      report.addSegmentScore(score);

   }
}
