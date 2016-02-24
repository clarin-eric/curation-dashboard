/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.component_registry.CCR_Constants;
import eu.clarin.cmdi.curation.component_registry.ComponentRegistryService;
import eu.clarin.cmdi.curation.component_registry.ProfileSpec;
import eu.clarin.cmdi.curation.entities.CMDIProfile;
import eu.clarin.cmdi.curation.report.CMDIProfileReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDIXPathService;

/**
 * @author dostojic
 *
 */
public class CMDIProfileValidator implements ProcessingStep<CMDIProfile, CMDIProfileReport> {

    private static final Logger _logger = LoggerFactory.getLogger(CMDIProfileValidator.class);

    @Override
    public boolean process(CMDIProfile entity, CMDIProfileReport report) {
	try {

	    CMDIXPathService xmlService = new CMDIXPathService(entity.getPath());

	    AutoPilot ap = new AutoPilot(xmlService.getNavigator());

	    // extract profileID:
	    ap.selectElement("ann:ID");
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
	    int numOfRequiredDatCat = 0;

	    xmlService.getNavigator().toElement(VTDNav.ROOT);
	    ap.selectElement("xs:element");
	    while (ap.iterate()) {
		numOfElements++;
		int datcatIndex = getDatcatIndex(xmlService.getNavigator());
		if (datcatIndex != -1) {
		    numOfElementsWithDatcat++;
		    // get the string
		    report.addDatcat(xmlService.getNavigator().toNormalizedString(datcatIndex));

		    int minOccInd = xmlService.getNavigator().getAttrVal("minOccurs");
		    if (minOccInd != -1) {
			if (!xmlService.getNavigator().toNormalizedString(minOccInd).isEmpty()
				&& !xmlService.getNavigator().toNormalizedString(minOccInd).equals("0"))
			    numOfRequiredDatCat++;
		    }
		}
	    }
	    report.numOfElements = numOfElements;
	    report.numOfElementsWithDatcat = numOfElementsWithDatcat;
	    report.numOfRequiredDatCat = numOfRequiredDatCat;
	    report.ratioOfElemenetsWithDatcat = (double) numOfElementsWithDatcat / numOfElements;

	    return true;

	} catch (Exception e) {
	    _logger.error("Error processing profile {}", entity.getPath(), e);
	    report.addDetail(Severity.FATAL, e.getMessage());
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

	report.numOfComponents = spec.getComponent().getComponents().size();
	report.numOfRequiredComponents = spec.getComponent().getComponents().stream()
		.mapToInt(c -> c.getCardinalityMin().equals("0") ? 0 : 1).filter(cardinlaityMin -> cardinlaityMin > 0)
		.sum();
	report.component = spec.getComponent().getComponents().stream().map(c -> {
	    if (c.getName() == null || c.getName().isEmpty())
		return c.getComponetId();
	    else if (c.getComponetId() == null || c.getComponetId().isEmpty())
		return c.getName();
	    else
		return c.getName() + "/" + c.getComponetId();
	}).collect(Collectors.toList());
    }

}
