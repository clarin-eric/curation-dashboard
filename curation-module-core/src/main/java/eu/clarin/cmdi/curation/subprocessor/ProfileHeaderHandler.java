package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.exception.ProfileNotFoundException;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;

public class ProfileHeaderHandler extends ProcessingStep<CMDProfile, CMDProfileReport> {

	@Override
	public void process(CMDProfile entity, CMDProfileReport report) throws Exception {
		String profileId;
		boolean isLocalFile = false;

		if (entity.getProfileId() != null && !entity.getProfileId().isEmpty()){
			profileId = entity.getProfileId();
		}else{
			//profileId = entity.getPath().toString();
			//isLocalFile = true;
		    throw new ProfileNotFoundException("can' find profile " + entity);
		}

		CRService service = new CRService();

		if(!isLocalFile)
			report.header = service.getPublicProfiles()
			.stream()
			.filter(h -> h.id.equals(profileId))
			.findFirst()
			.orElse(null);

		if(report.header == null){
			report.header = new ProfileHeader();
			report.header.id = profileId;
			report.header.cmdiVersion = entity.getCmdiVersion();


			report.header.isPublic = profileId == null? false : 
				service.getPublicProfiles().stream().filter(p -> p.id.equals(profileId)).findFirst().orElse(null) == null?
						false : true;		
		}

		report.header.isLocalFile = isLocalFile;


		if (!report.header.isPublic)
			addMessage(Severity.ERROR, "Profile is not public");

		if (!service.isNameUnique(report.header.name))
			addMessage(Severity.WARNING, "The name: " + report.header.name + " of the profile is not unique");

		if (!service.isDescriptionUnique(report.header.description))
			addMessage(Severity.WARNING,
					"The description: " + report.header.description + " of the profile is not unique");
	}

	@Override
	public Score calculateScore(CMDProfileReport report) {
		double score = report.header.isPublic ? 1.0 : 0;
		return new Score(score, 1.0, "header-section", msgs);
	}

}
