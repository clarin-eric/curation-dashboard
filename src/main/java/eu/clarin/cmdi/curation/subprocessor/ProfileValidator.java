/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.component_registry.CCR_Constants;
import eu.clarin.cmdi.curation.component_registry.ComponentRegistryService;
import eu.clarin.cmdi.curation.component_registry.ProfileSpec;
import eu.clarin.cmdi.curation.component_registry.ProfileSpec.CMD_Component;
import eu.clarin.cmdi.curation.entities.CMDIProfile;
import eu.clarin.cmdi.curation.report.CMDIProfileReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDIXPathService;

/**
 * @author dostojic
 *
 */
public class ProfileValidator extends ProcessingStep<CMDIProfile, CMDIProfileReport> {

    private static final Logger _logger = LoggerFactory.getLogger(ProfileValidator.class);

    @Override
    public boolean process(CMDIProfile entity, CMDIProfileReport report) {
	try {

	    CMDIXPathService xmlService = new CMDIXPathService(entity.getPath());

	    AutoPilot ap = new AutoPilot(xmlService.getNavigator());

	    // extract profileID:
	    ap.selectElement("ID");
	    while (ap.iterate()) {
		int t = xmlService.getNavigator().getText();
		if (t != -1) {
		    report.ID = xmlService.getNavigator().toNormalizedString(t)
			    .substring(CCR_Constants.PROFILE_PREFIX.length());
		    break;
		}
	    }

	    handleComponents(report);

	    int numOfElements = 0;
	    int numOfElementsWithDatcat = 0;

	    xmlService.getNavigator().toElement(VTDNav.ROOT);
	    ap.selectElement("element");
	    while (ap.iterate()) {
		boolean required = false;
		String datacat = null;
		numOfElements++;
		int datcatIndex = getDatcatIndex(xmlService.getNavigator());
		if (datcatIndex != -1) {
		    numOfElementsWithDatcat++;
		    // get the string
		    datacat = xmlService.getNavigator().toNormalizedString(datcatIndex);
		    int minOccInd = xmlService.getNavigator().getAttrVal("minOccurs");
		    if (minOccInd != -1) {
			if (!xmlService.getNavigator().toNormalizedString(minOccInd).isEmpty()
				&& !xmlService.getNavigator().toNormalizedString(minOccInd).equals("0"))
			    required = true;
		    }
		}
		
		if(datacat != null)
		    report.addDataCategory(datacat, required);
	    }
	    
	    report.numOfElements = numOfElements;
	    report.ratioOfElemenetsWithDatcat = (double) numOfElementsWithDatcat / numOfElements;

	    return true;

	} catch (Exception e) {
	    _logger.error("Error processing profile {}", entity.getPath(), e);
	    report.addDetail(Severity.FATAL, e.toString());
	    return false;
	}

    }

    private int getDatcatIndex(VTDNav vn) throws NavException {
	int result = -1;
	result = vn.getAttrValNS("http://www.isocat.org/ns/dcr", "datcat");
	if (result == -1) {
	    result = vn.getAttrValNS("http://www.isocat.org", "datcat");
	}
	if (result == -1) {
	    result = vn.getAttrVal("dcr:datcat");
	}
	return result;
    }

    private void handleComponents(CMDIProfileReport report) throws Exception {
	ProfileSpec spec = ComponentRegistryService.getInstance().getProfile(report.ID);
	report.name = spec.getHeader().getName();
	report.description = spec.getHeader().getDescription();

	report.isPublic = ComponentRegistryService.getInstance().isPublic(report.ID);

	for (CMD_Component c : spec.getComponent().getComponents()) {
	    report.addComponent(c.getName(), c.getComponetId(), !c.getCardinalityMin().equals("0"));
	}

    }

}
