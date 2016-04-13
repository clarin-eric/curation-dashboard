package eu.clarin.cmdi.curation.subprocessor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ICRService;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDXPathService;

public class InstanceHeaderValidator extends CMDSubprocessor {

	private static final Logger _logger = LoggerFactory.getLogger(InstanceHeaderValidator.class);

	private static final String PROFILE_ID_FORMAT = "clarin\\.eu:cr1:p_[0-9]+";
	private static final Pattern PROFILE_ID_PATTERN = Pattern.compile(PROFILE_ID_FORMAT);

	private CMDXPathService xmlService;

	private ICRService crService = CRService.getInstance();

	private String profile;
	

	@Override
	public boolean process(CMDInstance entity, CMDInstanceReport report) {
		boolean status = true;
		try {
			xmlService = new CMDXPathService(entity.getPath());
			profile = handleMdProfile(report);

			//at this point profile will be processed and cached
			report.profileScore = crService.getScore(profile);

			handleMdSelfLink(report);
			handleMdCollectionDisplyName(report);
			handleResourceProxies(report);
			xmlService = null;

			// exception happens when file can not be parsed or profileId is
			// missing
		} catch (Exception e) {
			report.isValid = false;
			_logger.error("Unable to process {}", entity.getPath(), e);
			addMessage(Severity.FATAL, "Unable to process header");
			status = false;
		} finally {
			report.addHeaderReport(profile, msgs);
		}

		return status;
	}

	private String handleMdProfile(CMDInstanceReport report) throws Exception {
		// extract profile from header MDProfile
		String profile = xmlService.getXPathValue("/CMD/Header/MdProfile/text()");
		String schema = getSchema();

		if(profile != null && !profile.matches(PROFILE_ID_FORMAT)){//try to normalize profileId
			addMessage(Severity.ERROR, "Format of the profile's ID in the element CMD/Header/MdProfile should should be: clarin.eu:cr1:p_xxxxxxxxxxxxx!");
			Matcher m = PROFILE_ID_PATTERN.matcher(profile);
			if(m.find())
				profile = m.group();
			else
				profile = null; //completly invalid value, set it to null and try to resolve profileId from schemaLocation
		}else if (profile == null)//if tag is missing add error msg, in the next if block we can't resolve if tag is missing or value was invalid
			addMessage(Severity.ERROR, "CMDI Record must contain CMD/Header/MdProfile element with the profile's ID in the format clarin.eu:cr1:p_xxxxxxxxxxxxx!");
		
		if (profile == null) {// element /CMD/Header/MdProfile/ is missing 
			report.mdProfileExists = false;

			// extract profile ID from schemaLocation attribute
			if (schema != null) {
				Matcher m = PROFILE_ID_PATTERN.matcher(schema);
				if (m.find())
					profile = m.group();
			}

			if (profile == null)
				throw new Exception("Profile can not be extracted from namespace!");
		}

		if (schema != null) {
			report.schemaAvailable = true;
			report.schemaInCCR = CRService.getInstance().isSchemaCRResident(schema);
		}

		return profile;

	}

	// try with schemaLocation/noNamespaceSchemaLocation attributes
	private String getSchema() throws Exception {
		String schema = null;
		VTDNav navigator = xmlService.getNavigator();
		navigator.toElement(VTDNav.ROOT);
		int index = navigator.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
		if (index != -1) {
			String[] schemaLocation = navigator.toNormalizedString(index).split(" ");
			schema = schemaLocation.length == 2? schemaLocation[1] : schemaLocation[0];
		} else {
			index = navigator.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation");
			if (index != -1)
				schema = navigator.toNormalizedString(index);
		}
		return schema;
	}

	private void handleMdCollectionDisplyName(CMDInstanceReport report) throws Exception {
		String mdCollectionDisplayName = xmlService.getXPathValue("/CMD/Header/MdCollectionDisplayName/text()");
		if (mdCollectionDisplayName == null || mdCollectionDisplayName.isEmpty()) {
			report.mdCollectionDispExists = false;
			addMessage(Severity.ERROR, "Value for MdCollectionDisplayName is missing");
		}
	}

	private void handleMdSelfLink(CMDInstanceReport report) throws Exception {
		String mdSelfLink = xmlService.getXPathValue("/CMD/Header/MdSelfLink/text()");
		if (mdSelfLink == null || mdSelfLink.isEmpty()) {
			addMessage(Severity.ERROR, "Value for MdSelfLink is missing");
			report.mdSelfLinkExists = false;
		} else {
			if (!CMDInstance.uniqueMDSelfLinks.add(mdSelfLink)) {
				CMDInstance.duplicateMDSelfLinks.add(mdSelfLink);
			}
		}
	}

	private void handleResourceProxies(CMDInstanceReport report) {
		int numOfResProxies = 0;
		int numOfResProxiesWithMime = 0;
		int numOfResProxiesWithReferences = 0;

		Map<String, Integer> resources = new HashMap<>();

		try {
			VTDNav nav = xmlService.getNavigator();
			nav.toElement(VTDNav.ROOT);
			AutoPilot ap = new AutoPilot(nav);
			ap.selectElement("ResourceProxy");
			while (ap.iterate()) {// for each ResourceProxy
				numOfResProxies++;

				// handle ResourceType
				if (xmlService.getNavigator().toElement(VTDNav.FIRST_CHILD, "ResourceType")) {

					String type = xmlService.getNavigator().toNormalizedString(xmlService.getNavigator().getText());
					if (!type.isEmpty())
						resources.put(type, resources.containsKey(type) ? resources.get(type) + 1 : 1);

					// handle mimeType

					int ind;
					if ((ind = xmlService.getNavigator().getAttrVal("mimetype")) != -1
							&& !xmlService.getNavigator().toNormalizedString(ind).isEmpty())
						numOfResProxiesWithMime++;

				}

				// handle ResourceRef
				if (xmlService.getNavigator().toElement(VTDNav.NEXT_SIBLING, "ResourceRef")) {
					String reference = xmlService.getNavigator()
							.toNormalizedString(xmlService.getNavigator().getText());
					if (!reference.isEmpty())
						numOfResProxiesWithReferences++;
				}

				xmlService.getNavigator().toElement(VTDNav.PARENT);

			} // end while
		} catch (Exception e) {
			addMessage(Severity.FATAL, e.getMessage());
			report.isValid = false;
		} finally {
			report.addResProxyReport(numOfResProxies, numOfResProxiesWithMime, numOfResProxiesWithReferences,
					resources);
		}
	}

}
