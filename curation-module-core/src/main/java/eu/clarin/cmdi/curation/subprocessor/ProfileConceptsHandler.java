/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */
public class ProfileConceptsHandler extends ProcessingStep<CMDProfile, CMDProfileReport> {

	private static final Logger _logger = LoggerFactory.getLogger(ProfileConceptsHandler.class);

	@Override
	public boolean process(CMDProfile entity, CMDProfileReport report) {
		try {

			// createElementsReport(int total, int unique, int required, int
			// withDatacategory, Map<String, Integer> datcats);
			int numOfElements = 0;
			int numOfReqElements = 0;
			int numOfElemsWithDatcat = 0;
			int numOfReqDatcats = 0;
			Map<String, Integer> elements = new HashMap<>();
			Map<String, Integer> datcats = new HashMap();

			VTDNav xsdNavigator = CRService.getInstance().getParsedXSD(entity.getProfile());
			AutoPilot ap = new AutoPilot(xsdNavigator);
			ap.selectElement("element");
			while (ap.iterate()) {
				int ind = xsdNavigator.getAttrVal("name");

				if (ind != -1) {
					numOfElements++;
					String name = xsdNavigator.toNormalizedString(ind);
					elements.put(name, elements.containsKey(name) ? elements.get(name) + 1 : 1);

				} else
					_logger.error("missing name");

				ind = xsdNavigator.getAttrVal("minOccurs");

				// if attr CardinalityMin > 0
				boolean required = false;
				if (ind != -1 && xsdNavigator.toNormalizedString(ind).compareTo("0") > 0) {
					numOfReqElements++;
					required = true;
				}

				ind = getDatcatIndex(xsdNavigator);
				if (ind != -1) {
					numOfElemsWithDatcat++;
					if (required)
						numOfReqDatcats++;
					String datcatUrl = xsdNavigator.toNormalizedString(ind);
					datcats.put(datcatUrl, datcats.containsKey(datcatUrl) ? datcats.get(datcatUrl) + 1 : 1);
				}
			}

			report.createElementsReport(numOfElements, elements.size(), numOfReqElements, numOfElemsWithDatcat, datcats,
					numOfReqDatcats);

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
			result = vn.getAttrVal("datcat");
		}
		return result;
	}
}
