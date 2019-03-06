package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.exception.ProfileNotFoundException;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;

public class ProfileHeaderHandler extends ProcessingStep<CMDProfile, CMDProfileReport> {

	@Override
	public void process(CMDProfile entity, CMDProfileReport report) throws ProfileNotFoundException {
		String schemaLocation;
		boolean isLocalFile = false;

		if (entity.getSchemaLocation() != null && !entity.getSchemaLocation().isEmpty()){
			schemaLocation = entity.getSchemaLocation();
		}else{
			//profileId = entity.getPath().toString();
			//isLocalFile = true;
		    throw new ProfileNotFoundException("can' find profile " + entity);
		}

		CRService service = new CRService();

		if(!isLocalFile && schemaLocation.startsWith(Configuration.VLO_CONFIG.getComponentRegistryRESTURL()))
			report.header = service.getPublicProfiles()
			.stream()
			.filter(h -> h.getSchemaLocation().equals(schemaLocation))
			.findFirst()
			.orElse(null);
		

		if(report.header == null){
			report.header = new ProfileHeader();
			report.header.setSchemaLocation(schemaLocation);
			report.header.setUrl(Configuration.BASE_URL+"rest/profile/"+report.getName()+".xml");
			report.header.setCmdiVersion(entity.getCmdiVersion());


			report.header.setPublic(false);
		}

		report.header.setLocalFile(isLocalFile);

		report.url = Configuration.BASE_URL + "rest/profile/" + report.header.getId() + ".xml";


		if (!report.header.isPublic())
			addMessage(Severity.ERROR, "Profile is not public");

		if (!service.isNameUnique(report.header.getName()))
			addMessage(Severity.WARNING, "The name: " + report.header.getName() + " of the profile is not unique");

		if (!service.isDescriptionUnique(report.header.getDescription()))
			addMessage(Severity.WARNING,
					"The description: " + report.header.getDescription() + " of the profile is not unique");
	}

	@Override
	public Score calculateScore(CMDProfileReport report) {
		double score = report.header.isPublic() ? 1.0 : 0;
		return new Score(score, 1.0, "header-section", msgs);
	}

}
