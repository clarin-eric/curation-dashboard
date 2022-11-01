/**
 *
 */
package eu.clarin.cmdi.curation.processor;

import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.exception.SubprocessorException;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.subprocessor.ext.ProfileElementsHandler;
import eu.clarin.cmdi.curation.subprocessor.ext.ProfileFacetHandler;
import eu.clarin.cmdi.curation.subprocessor.ext.ProfileHeaderHandler;
import lombok.extern.slf4j.Slf4j;
/**
 *
 */
@Slf4j
public class CMDProfileProcessor {

    public CMDProfileReport process(CMDProfile profile){

        CMDProfileReport report = new CMDProfileReport();
        
        try {

           ProfileHeaderHandler profileHeaderHandler = new ProfileHeaderHandler();
           profileHeaderHandler.process(profile, report);
           report.addSegmentScore(profileHeaderHandler.calculateScore(report));
   
           ProfileElementsHandler profileElementsHandler = new ProfileElementsHandler();
           profileElementsHandler.process(report);
           report.addSegmentScore(profileElementsHandler.calculateScore(report));
   
           ProfileFacetHandler profileFacetHandler = new ProfileFacetHandler();
           profileFacetHandler.process(report);
           report.addSegmentScore(profileFacetHandler.calculateScore(report));
        }
        catch(SubprocessorException ex) {
           
           log.debug("", ex);
        }

        return report;

    }

}
