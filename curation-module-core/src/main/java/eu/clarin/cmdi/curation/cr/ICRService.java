package eu.clarin.cmdi.curation.cr;

import java.util.Collection;

import javax.xml.validation.Schema;

import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;

public interface ICRService {
	
	public ProfileHeader createProfileHeader(String schemaLocation, String cmdiVersion, boolean isLocalFile);

	ParsedProfile getParsedProfile(ProfileHeader header) throws Exception;

	Schema getSchema(ProfileHeader header) throws Exception;

	boolean isNameUnique(String name) throws Exception;

	boolean isDescriptionUnique(String description);

	boolean isSchemaCRResident(String schemaLocation);

	Collection<ProfileHeader> getPublicProfiles();
	
}
