package eu.clarin.cmdi.curation.subprocessor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.exception.ExceptionUtils;
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

public class InstanceHeaderValidatior extends CMDSubprocessor {

	private static final Logger _logger = LoggerFactory.getLogger(InstanceHeaderValidatior.class);

	private static final Pattern PROFILE_ID_PATTERN = Pattern.compile(".*(clarin.eu:cr1:p_[0-9]+).*");

	private CMDXPathService xmlService;
	
	private ICRService crService = CRService.getInstance();
	
	private String profile;

	@Override
	public boolean process(CMDInstance entity, CMDInstanceReport report) {
		boolean status = true;
		try {
			xmlService = new CMDXPathService(entity.getPath());
			profile = handleMdProfile(report);
			
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

		if (profile == null) {// not in header
			report.mdProfileExists = false;
			addMessage(Severity.ERROR, "CMDI Record must contain CMD/Header/MdProfile tag with profile ID!");

			if (!schema.startsWith(CRService.getInstance().REST_API))

				// extract profile ID
				if (schema != null) {
					Matcher m = PROFILE_ID_PATTERN.matcher(schema);
					if (m.find())
						profile = m.group(1);
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
			schema = navigator.toNormalizedString(index).split(" ")[1];
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
			addMessage(Severity.ERROR, "Value for MdCollctionDisplayName is missing");
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
