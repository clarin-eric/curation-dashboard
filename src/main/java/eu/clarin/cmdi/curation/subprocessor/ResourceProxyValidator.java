/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

import com.ximpleware.AutoPilot;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */
public class ResourceProxyValidator implements ProcessingActivity<CMDIRecord> {

    @Override
    public Severity process(CMDIRecord entity) {
	int numOfResWithMime = 0;
	int numOfTypeLandingPage = 0;
	int numOfTypeLandingPageWithoutLink = 0;
	int numOfTypeRes = 0;
	int numOfTypeMD = 0;
	
	try {
	    Collection<ResourceProxy> resources = new LinkedList<>();
	    VTDNav navigator = parse(entity.getPath());
	    AutoPilot ap = new AutoPilot(navigator);
	    ap.selectXPath("//ResourceProxy");
	    int index;
	    while ((index = ap.evalXPath()) != -1) {// for each ResourceProxy
		
		ResourceProxy resource = new ResourceProxy();
		int ind;
		if ((ind = navigator.getAttrVal("id")) != -1)
		    resource.id = navigator.toNormalizedString(ind);

		if (navigator.toElement(VTDNav.FIRST_CHILD, "ResourceType")) {
		    resource.resourceType = navigator.toNormalizedString(navigator.getText());

		    if ((ind = navigator.getAttrVal("mimetype")) != -1)
			resource.mimeType = navigator.toNormalizedString(ind);
		}

		if (navigator.toElement(VTDNav.NEXT_SIBLING, "ResourceRef"))
		    resource.resourceRef = navigator.toNormalizedString(navigator.getText());

		navigator.toElement(VTDNav.PARENT);

		resources.add(resource);
	    } // end while

	    for (ResourceProxy res : resources) {
		if (!res.mimeType.isEmpty())
		    numOfResWithMime++;
		if (res.resourceType.equals("Resource"))
		    numOfTypeRes++;
		else if (res.resourceType.equals("Metadata"))
		    numOfTypeMD++;

		// handle LandingPage
		if (res.resourceType.equals("LandingPage")) {
		    numOfTypeLandingPage++;
		    if (res.resourceRef.isEmpty())
			numOfTypeLandingPageWithoutLink++;

		}
	    }

	    entity.setNumOfResources(resources.size());
	    entity.setNumOfResWithMime(numOfResWithMime);
	    entity.setNumOfLandingPages(numOfTypeLandingPage);
	    entity.setNumOfLandingPagesWithoutLink(numOfTypeLandingPageWithoutLink);
	    entity.setNumOfResources(numOfTypeRes);
	    entity.setNumOfMetadata(numOfTypeMD);

	    return Severity.NONE;

	} catch (Exception e) {
	    entity.addDetail(Severity.FATAL, e.getMessage());
	    return Severity.FATAL;
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

    private class ResourceProxy {

	String id = "";
	String mimeType = "";
	String resourceType = "";
	String resourceRef = "";

	@Override
	public String toString() {
	    return (id.isEmpty() ? "" : "Id: " + id + "\t")
		    + (mimeType.isEmpty() ? "" : "MIME type: " + mimeType + "\t")
		    + (resourceType.isEmpty() ? "" : "ResourceType:" + resourceType + "\t")
		    + (resourceType.isEmpty() ? "" : "ResourceRef:" + resourceRef + "\t");
	}

    }

}
