package eu.clarin.cmdi.curation.subprocessor;

import java.util.regex.Matcher;

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
		String schemaLocation;
		boolean isLocalFile = false;
		
		if (entity.getSchemaLocation() != null && !entity.getSchemaLocation().isEmpty()){
			schemaLocation = entity.getSchemaLocation();
		}else{
			schemaLocation = entity.getPath().toString();
			isLocalFile = true;
		}
		
		CRService service = new CRService();
		
		if(!isLocalFile && service.isSchemaCRResident(schemaLocation))
			report.header = service.getPublicProfiles()
			.stream()
			.filter(h -> h.schemaLocation.equals(schemaLocation))
			.findFirst()
			.orElse(null);
		
		if(report.header == null){
			report.header = new ProfileHeader();
			report.header.schemaLocation = schemaLocation;
			report.header.cmdiVersion = entity.getCmdiVersion();
			
			Matcher matcher = CRService.PROFILE_ID_PATTERN.matcher(report.header.schemaLocation);
			if(!matcher.find())
				throw new ProfileNotFoundException("schema " + report.header.schemaLocation + " is no valid CMD profile!" );
			String id = matcher.group(0);
			
			report.header.isPublic = id == null? false : 
				service.getPublicProfiles().stream().filter(p -> p.id.equals(id)).findFirst().orElse(null) == null?
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
