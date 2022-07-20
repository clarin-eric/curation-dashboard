/**
 *
 */
package eu.clarin.cmdi.curation.processor;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.exception.ProfileNotFoundException;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.subprocessor.ProfileElementsHandler;
import eu.clarin.cmdi.curation.subprocessor.ProfileFacetHandler;
import eu.clarin.cmdi.curation.subprocessor.ProfileHeaderHandler;
/**
 *
 */
public class CMDProfileProcessor {

    public CMDProfileReport process(CMDProfile profile) throws ProfileNotFoundException, ExecutionException, IOException {

        CMDProfileReport report = new CMDProfileReport();

        ProfileHeaderHandler profileHeaderHandler = new ProfileHeaderHandler();
        profileHeaderHandler.process(profile, report);
        report.addSegmentScore(profileHeaderHandler.calculateScore(report));

        ProfileElementsHandler profileElementsHandler = new ProfileElementsHandler();
        profileElementsHandler.process(report);
        report.addSegmentScore(profileElementsHandler.calculateScore(report));

        ProfileFacetHandler profileFacetHandler = new ProfileFacetHandler();
        profileFacetHandler.process(report);
        report.addSegmentScore(profileFacetHandler.calculateScore(report));

        return report;

    }

}
