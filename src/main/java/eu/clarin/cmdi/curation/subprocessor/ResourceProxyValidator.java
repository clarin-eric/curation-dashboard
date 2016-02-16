/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */
public class ResourceProxyValidator extends CMDISubprocessor {

    static final Logger _logger = LoggerFactory.getLogger(ResourceProxyValidator.class);

    @Override
    public boolean process(CMDIInstance entity, CMDIInstanceReport report) {
	try {
	    VTDNav navigator = parse(entity.getPath());
	    AutoPilot ap = new AutoPilot(navigator);
	    ap.selectXPath("//ResourceProxy");
	    int index;
	    while ((index = ap.evalXPath()) != -1) {// for each ResourceProxy

		report.numOfResProxies++;
		int ind;

		if (navigator.toElement(VTDNav.FIRST_CHILD, "ResourceType")) {

		    // handle ResourceType
		    String resourceType = navigator.toNormalizedString(navigator.getText());
		    switch (resourceType) {
		    case "Resource":
			report.numOfResources++;
			break;
		    case "Metadata":
			report.numOfMetadata++;
			break;
		    case "LandingPage":
			report.numOfLandingPages++;
			if (navigator.toElement(VTDNav.NEXT_SIBLING, "ResourceRef")
				&& !navigator.toNormalizedString(navigator.getText()).isEmpty()) {
			    report.numOfLandingPagesWithoutLink++;
			}
			break;
		    default:
			_logger.debug("Unhandeled value for ResourceType: {}", resourceType);

		    }

		    // handle mimeType
		    if ((ind = navigator.getAttrVal("mimetype")) != -1 && !navigator.toNormalizedString(ind).isEmpty())
			report.numOfResWithMime++;
		}

		navigator.toElement(VTDNav.PARENT);

	    } // end while

	    return true;

	} catch (Exception e) {
	    report.addDetail(Severity.FATAL, e.getMessage());
	    return false;
	}
    }

    private VTDNav parse(Path cmdiRecord) throws Exception {
	VTDGen parser = new VTDGen();
	try {
	    parser.setDoc(Files.readAllBytes(cmdiRecord));
	    parser.parse(true);
	    VTDNav navigator = parser.getNav();
	    parser = null;
	    return navigator;
	} catch (IOException | ParseException e) {
	    throw new Exception("Errors while parsing " + cmdiRecord, e);
	}

    }

    // private class ResourceProxy {
    //
    // String id = "";
    // String mimeType = "";
    // String resourceType = "";
    // String resourceRef = "";
    //
    // @Override
    // public String toString() {
    // return (id.isEmpty() ? "" : "Id: " + id + "\t")
    // + (mimeType.isEmpty() ? "" : "MIME type: " + mimeType + "\t")
    // + (resourceType.isEmpty() ? "" : "ResourceType:" + resourceType + "\t")
    // + (resourceType.isEmpty() ? "" : "ResourceRef:" + resourceRef + "\t");
    // }
    //
    // }

}
