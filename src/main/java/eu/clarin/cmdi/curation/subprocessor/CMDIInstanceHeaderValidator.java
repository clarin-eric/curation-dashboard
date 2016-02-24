package eu.clarin.cmdi.curation.subprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.component_registry.CCR_Constants;
import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDIXPathService;

public class CMDIInstanceHeaderValidator extends CMDISubprocessor {

    private static final Logger _logger = LoggerFactory.getLogger(CMDIInstanceHeaderValidator.class);

    private static final Pattern PROFILE_ID_PATTERN = Pattern.compile(".*(clarin.eu:cr1:p_[0-9]+).*");

    private CMDIXPathService xmlService;
    
    
    @Override
    public boolean process(CMDIInstance entity, CMDIInstanceReport report) {
	boolean status = true;
	String profile = null;
	try {
	    xmlService = new CMDIXPathService(entity.getPath());	    
	    profile = handleMdProfile(report);
	    handleMdSelfLink(report);
	    handleMdCollectionDisplyName(report);
	    xmlService = null;

	    // we are throwing it only if file can not be parsed or profileId is
	    // missing
	} catch (Exception e) {	    
	    addMessage(Severity.FATAL, e.getMessage());
	    status =  false;
	}finally{
	    report.addHeaderReport(profile, msgs);
	}
	
	
	return status;
    }

    private String handleMdProfile(CMDIInstanceReport report) throws Exception {
	// search in header first
	String profile = xmlService.xpath("/CMD/Header/MdProfile/text()");
	if (profile == null) {// not in header
	    report.mdProfileExists = false;
	    addMessage(Severity.ERROR, "CMDI Record must contain CMD/Header/MdProfile tag with proile ID!");
	    profile = extractProfileFromNameSpace();
	    if (profile == null)
		throw new Exception("Profile can not be extracted from namespace!");
	}
	// keep profile without prefix "clarin.eu:cr1"
	report.schemaAvailable = true;
	if(profile.startsWith(CCR_Constants.PROFILE_PREFIX)){
	    report.schemaInCCR = true;
	    profile = profile.substring(CCR_Constants.PROFILE_PREFIX.length());
	}
	return profile;
    }

    // try with schemaLocation/noNamespaceSchemaLocation attributes
    private String extractProfileFromNameSpace() throws Exception {
	String schema = null;
	VTDNav navigator = xmlService.getNavigator();
	navigator.toElement(VTDNav.ROOT);
	int index = navigator.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
	if (index != -1) {
	    schema = navigator.toNormalizedString(index).split(" ")[1];
	} else {
	    index = navigator.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation");
	    if (index != -1)
		schema = navigator.toNormalizedString(index);
	}
	// extract profile ID
	if (schema != null) {
	    Matcher m = PROFILE_ID_PATTERN.matcher(schema);
	    if (m.find())
		schema = m.group(1);
	}
	return schema;
    }

    private void handleMdCollectionDisplyName(CMDIInstanceReport report) throws Exception {
	String mdCollectionDisplayName = xmlService.xpath("/CMD/Header/MdCollectionDisplayName/text()");
	if (mdCollectionDisplayName == null || mdCollectionDisplayName.isEmpty()){
	    report.mdCollectionDispExists = false;
	    addMessage(Severity.ERROR, "Value for MdCollectionDisplayName is missing");
	}
    }

    private void handleMdSelfLink(CMDIInstanceReport report) throws Exception {
	String mdSelfLink = xmlService.xpath("/CMD/Header/MdSelfLink/text()");
	if (mdSelfLink == null || mdSelfLink.isEmpty()){
	    addMessage(Severity.ERROR, "Value for MdCollctionDisplayName is missing");
	    report.mdSelfLinkExists = false;
	}
	else {
	    if (!CMDIInstance.uniqueMDSelfLinks.add(mdSelfLink)) {		
		CMDIInstance.duplicateMDSelfLinks.add(mdSelfLink);
	    }
	}
    }


}
