/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.cr.CRConstants;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileSpec;
import eu.clarin.cmdi.curation.cr.ProfileSpec.CMD_Component;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDXPathService;

/**
 * @author dostojic
 *
 */
public class ProfileElementsHandler extends ProcessingStep<CMDProfile, CMDProfileReport> {

    private static final Logger _logger = LoggerFactory.getLogger(ProfileElementsHandler.class);

    @Override
    public boolean process(CMDProfile entity, CMDProfileReport report) {
	try {
	    
	    //createElementsReport(int total, int unique, int required, int withDatacategory, Map<String, Integer> datcats);
	    int numOfElements = 0;
	    int numOfReqElements = 0;
	    int numOfElemsWithDatcat = 0;
	    int numOfReqDatcats = 0;
	    Map<String, Integer> elements = new HashMap<>();
	    Map<String, Integer> datcats = new HashMap();

	    CMDXPathService xmlService = new CMDXPathService(CRConstants.getProfilesURL(entity.getProfile()) + "xsd");
	    VTDNav navigator = xmlService.getNavigator();
	    AutoPilot ap = new AutoPilot(xmlService.getNavigator());

	   
	    xmlService.getNavigator().toElement(VTDNav.ROOT);
	    ap.selectElement("element");
	    while (ap.iterate()) {		
		int ind = navigator.getAttrVal("name");

		if (ind != -1) {
		    numOfElements++;
		    String name = navigator.toNormalizedString(ind);
		    elements.put(name, elements.containsKey(name) ? elements.get(name) + 1 : 1);
		    
		} else
		    _logger.error("missing name");
		
		
		ind = navigator.getAttrVal("minOccurs");
		
		// if attr CardinalityMin > 0
		boolean required = false;
		if (ind != -1 && navigator.toNormalizedString(ind).compareTo("0") > 0){
		    numOfReqElements++;
		    required = true;
		}

		
		
		ind = getDatcatIndex(navigator);
		if (ind != -1) {
		    numOfElemsWithDatcat++;
		    if(required)
			numOfReqDatcats++;
		    String datcatUrl = navigator.toNormalizedString(ind);
		    datcats.put(datcatUrl, datcats.containsKey(datcatUrl) ? datcats.get(datcatUrl) + 1 : 1);
		}
	    }
	    
	    report.createElementsReport(numOfElements, elements.size(), numOfReqElements, numOfElemsWithDatcat, datcats, numOfReqDatcats);
	    
	    elements = null;
	    datcats = null;

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
}
