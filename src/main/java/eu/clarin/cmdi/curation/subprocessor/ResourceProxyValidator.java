
package eu.clarin.cmdi.curation.subprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDIXPathService;

/**
 * @author dostojic
 *
 */
public class ResourceProxyValidator extends CMDISubprocessor {

    static final Logger _logger = LoggerFactory.getLogger(ResourceProxyValidator.class);

    @Override
    public boolean process(CMDIInstance entity, CMDIInstanceReport report) {
	try {

	    CMDIXPathService xmlService = new CMDIXPathService(entity.getPath());
	    AutoPilot ap = new AutoPilot(xmlService.getNavigator());
	    ap.selectElement("ResourceProxy");
	    while (ap.iterate()) {// for each ResourceProxy
		report.numOfResProxies++;

		if (xmlService.getNavigator().toElement(VTDNav.FIRST_CHILD, "ResourceType")) {

		    // handle ResourceType
		    String resourceType = xmlService.getNavigator()
			    .toNormalizedString(xmlService.getNavigator().getText());
		    switch (resourceType) {
		    case "Resource":
			report.numOfResources++;
			break;
		    case "Metadata":
			report.numOfMetadata++;
			break;
		    case "LandingPage":
			report.numOfLandingPages++;
			if (xmlService.getNavigator().toElement(VTDNav.NEXT_SIBLING, "ResourceRef") && !xmlService
				.getNavigator().toNormalizedString(xmlService.getNavigator().getText()).isEmpty()) {
			    report.numOfLandingPagesWithoutLink++;
			}
			break;
		    default:
			_logger.debug("Unhandeled value for ResourceType: {}", resourceType);

		    }

		    int ind;
		    // handle mimeType
		    if ((ind = xmlService.getNavigator().getAttrVal("mimetype")) != -1
			    && !xmlService.getNavigator().toNormalizedString(ind).isEmpty())
			report.numOfResWithMime++;
		}

		xmlService.getNavigator().toElement(VTDNav.PARENT);

	    } // end while

	    return true;

	} catch (Exception e) {
	    report.addDetail(Severity.FATAL, e.getMessage());
	    return false;
	}
    }
}
