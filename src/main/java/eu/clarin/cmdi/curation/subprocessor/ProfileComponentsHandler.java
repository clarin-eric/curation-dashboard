/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ICRService;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDXPathService;

/**
 * @author dostojic
 *
 */
public class ProfileComponentsHandler extends ProcessingStep<CMDProfile, CMDProfileReport> {

	private static final Logger _logger = LoggerFactory.getLogger(ProfileComponentsHandler.class);

	@Override
	public boolean process(CMDProfile entity, CMDProfileReport report) {
		try {
			ICRService crService = CRService.getInstance();
			VTDNav navigator = crService.getParseXML(entity.getProfile());
			CMDXPathService xmlService = new CMDXPathService(navigator);			

			// header - moved to profileHeaderHandler
//			report.ID = entity.getProfile();
//			report.name = xmlService.getXPathValue("/CMD_ComponentSpec/Header/Name/text()");
//			report.description = xmlService.getXPathValue("/CMD_ComponentSpec/Header/Description/text()");			
//			report.isPublic = crService.isPublic(report.ID);			

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
