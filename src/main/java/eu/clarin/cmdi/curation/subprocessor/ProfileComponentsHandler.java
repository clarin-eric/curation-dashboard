/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.cr.CRConstants;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.IComponentRegistryService;
import eu.clarin.cmdi.curation.entities.CMDIProfile;
import eu.clarin.cmdi.curation.report.CMDIProfileReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDIXPathService;

/**
 * @author dostojic
 *
 */
public class ProfileComponentsHandler extends ProcessingStep<CMDIProfile, CMDIProfileReport> {

    private static final Logger _logger = LoggerFactory.getLogger(ProfileComponentsHandler.class);

    @Override
    public boolean process(CMDIProfile entity, CMDIProfileReport report) {
	try {

	    CMDIXPathService xmlService = new CMDIXPathService(CRConstants.getProfilesURL(entity.getProfile()) + "xml");
	    VTDNav navigator = xmlService.getNavigator();

	    // header
	    //report.ID = xmlService.xpath("/CMD_ComponentSpec/Header/ID/text()");
	    report.ID = entity.getProfile();
	    report.name = xmlService.xpath("/CMD_ComponentSpec/Header/Name/text()");
	    report.description = xmlService.xpath("/CMD_ComponentSpec/Header/Description/text()");
	    
	    IComponentRegistryService crService = CRService.getInstance();
	    report.isPublic = crService.isPublic(report.ID);

	    int numOfComponents = 0;
	    int numOfRequiredComponents = 0;
	    int numOfUniqueComponents = 0;
	    
	    Map<String, Integer> components = new HashMap<>();

	    AutoPilot ap = new AutoPilot(navigator);

	    // components
	    navigator.toElement(VTDNav.ROOT);
	    ap.selectElement("CMD_Component");
	    while (ap.iterate()) {
		numOfComponents++;
		int ind;
		ind = navigator.getAttrVal("CardinalityMin");

		// if attr CardinalityMin > 0
		if (ind != -1 && navigator.toNormalizedString(ind).compareTo("0") > 0)
		    numOfRequiredComponents++;

		ind = navigator.getAttrVal("name");

		if (ind != -1) {
		    String name = navigator.toNormalizedString(ind);
		    components.put(name, components.containsKey(name) ? components.get(name) + 1 : 1);
		} else
		    _logger.error("missing name");
	    }

	    
	    numOfUniqueComponents = components.keySet().size();
	    components = null;
	    
	    report.createComponentsReport(numOfComponents, numOfRequiredComponents, numOfUniqueComponents);

	    return true;

	} catch (Exception e) {
	    _logger.error("Error processing profile {}", entity.getPath(), e);
	    report.addDetail(Severity.FATAL, e.toString());
	    return false;
	}
    }


}
