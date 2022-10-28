package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.CRServiceImpl;
import eu.clarin.cmdi.curation.cr.ProfileDescription;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.exception.ProfileNotFoundException;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.vlo.config.VloConfig;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

public class ProfileHeaderHandler {
   
   @Autowired
   private CRService crService;
   @Autowired
   private VloConfig vloConf;

	protected Collection<Message> msgs = null;

    public void process(CMDProfile entity, CMDProfileReport report) throws Exception {
        String schemaLocation;
        boolean isLocalFile = false;

        if (entity.getSchemaLocation() != null && !entity.getSchemaLocation().isEmpty()) {
            schemaLocation = entity.getSchemaLocation();
        } else {
            //profileId = entity.getPath().toString();
            //isLocalFile = true;
            throw new ProfileNotFoundException("can' find profile " + entity);
        }

        if (schemaLocation.startsWith(vloConf.getComponentRegistryRESTURL()))
            report.header = crService.getPublicProfiles()
                    .stream()
                    .filter(h -> h.getSchemaLocation().equals(schemaLocation))
                    .findFirst()
                    .orElse(null);


        if (report.header == null) {
            report.header = new ProfileDescription();
            report.header.setSchemaLocation(schemaLocation);
            report.header.setCmdiVersion(entity.getCmdiVersion());


            report.header.setPublic(false);
        }

        report.header.setLocalFile(isLocalFile);

        if (!report.header.isPublic())
            addMessage(Severity.ERROR, "Profile is not public");

        if (!crService.isNameUnique(report.header.getName()))
            addMessage(Severity.WARNING, "The name: " + report.header.getName() + " of the profile is not unique");

        if (!crService.isDescriptionUnique(report.header.getDescription()))
            addMessage(Severity.WARNING,
                    "The description: " + report.header.getDescription() + " of the profile is not unique");
    }

    public Score calculateScore(CMDProfileReport report) {
        double score = report.header.isPublic() ? 1.0 : 0;
        return new Score(score, 1.0, "header-section", msgs);
    }

	protected void addMessage(Severity lvl, String message) {
		if (msgs == null) {
			msgs = new ArrayList<>();
		}
		msgs.add(new Message(lvl, message));
	}

}
