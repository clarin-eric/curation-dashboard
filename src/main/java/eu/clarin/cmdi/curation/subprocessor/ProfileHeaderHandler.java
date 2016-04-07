package eu.clarin.cmdi.curation.subprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ICRService;
import eu.clarin.cmdi.curation.cr.ProfileDescriptions.ProfileHeader;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Severity;

public class ProfileHeaderHandler extends ProcessingStep<CMDProfile, CMDProfileReport> {

	private static final Logger _logger = LoggerFactory.getLogger(ProfileComponentsHandler.class);

	@Override
	public boolean process(CMDProfile entity, CMDProfileReport report) {
		try {
			ICRService crService = CRService.getInstance();
			ProfileHeader header = crService.getProfileHeader(entity.getProfile());

			report.createHeaderReport(header.getId(), CRService.REST_API + header.getId(), header.getName(), header.getDescription(), header.isPublic());
			
			if(!crService.isNameUnique(header.getName()))
				report.addDetail(Severity.WARNING, "The name: " + header.getName() + " of the profile is not unique");
			
			if(!crService.isDescriptionUnique(header.getDescription()))
				report.addDetail(Severity.WARNING, "The description: " + header.getDescription() + " of the profile is not unique");

			return true;

		} catch (Exception e) {
			_logger.error("Error processing profile {}", entity.getPath(), e);
			report.addDetail(Severity.FATAL, e.toString());
			return false;
		}
	}

}
