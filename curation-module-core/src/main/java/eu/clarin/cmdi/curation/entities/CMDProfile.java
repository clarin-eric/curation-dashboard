package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDProfileProcessor;

/**
 * @author dostojic
 *
 */

public class CMDProfile extends CurationEntity {

	private String profileId;
	private String cmdiVersion;

	public CMDProfile(String profileId, String cmdiVersion) {
		super(null);
		this.profileId = profileId;
		this.cmdiVersion = cmdiVersion;
	}

/*	public CMDProfile(Path path, String cmdiVersion) {
		super(path);
		this.cmdiVersion = cmdiVersion;
	}	*/
	

	public String getProfileId() {
        return profileId;
    }

    public String getCmdiVersion() {
		return cmdiVersion;
	}

	@Override
	protected AbstractProcessor getProcessor() {
		return new CMDProfileProcessor();
	}
	
	@Override
	public String toString() {
		return "Profile: " + (path != null? path.toString() : Configuration.VLO_CONFIG.getComponentRegistryProfileSchema(profileId));
	}

}
