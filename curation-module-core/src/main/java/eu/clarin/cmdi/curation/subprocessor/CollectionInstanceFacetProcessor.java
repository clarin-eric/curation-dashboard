package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import eu.clarin.cmdi.curation.entities.CMDInstance;

import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;
import eu.clarin.cmdi.curation.vlo_extensions.FacetMappingCacheFactory;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;


public class CollectionInstanceFacetProcessor extends CMDSubprocessor {


    @Override
    public void process(CMDInstance entity, CMDInstanceReport report) throws IOException, ExecutionException {

        Map<String, List<ValueSet>> facetValuesMap = entity.getCMDIData().getDocument();

        entity.setParsedInstance(null);


        FacetMapping facetMapping;
        try {
            facetMapping = FacetMappingCacheFactory.getInstance().getFacetMapping(report.header);

            report.facets = new FacetReportCreator().createFacetReport(report.header, facetMapping);

            int numOfCoveredByIns = 0;

            for (Coverage coverage : report.facets.coverage) {
                if (!coverage.coveredByProfile)
                    continue; //we have to discuss if this should still be the case

                coverage.coveredByInstance = (facetValuesMap.get(coverage.name) != null && !facetValuesMap.get(coverage.name).isEmpty() && !facetValuesMap.get(coverage.name).get(0).getValue().isEmpty());
                if (coverage.coveredByInstance)
                    numOfCoveredByIns++;
            }

            report.facets.instanceCoverage = report.facets.numOfFacets == 0 ? 0.0 : (numOfCoveredByIns / (double) report.facets.numOfFacets); //cast to double to get a double as result

        } finally {
            entity.setCMDIData(null);
            entity.setParsedInstance(null);
        }
        ;

    }

    @Override
    public Score calculateScore(CMDInstanceReport report) {
        return new Score(report.facets.instanceCoverage, 1.0, "facet-mapping", msgs);
    }
}
