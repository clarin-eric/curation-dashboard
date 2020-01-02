/**
 *
 */
package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.vlo_extensions.FacetMappingCacheFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class ProfileFacetHandler {

    protected Collection<Message> msgs = null;

    public void process(CMDProfileReport report) throws IOException, ExecutionException {

        FacetMapping facetMapping;
        facetMapping = FacetMappingCacheFactory.getInstance().getFacetMapping(report.header);
        report.facet = new FacetReportCreator().createFacetReport(report.header, facetMapping);

    }

    public Score calculateScore(CMDProfileReport report) {
        return new Score(report.facet.profileCoverage, 1.0, "facets-section", msgs);
    }

}
