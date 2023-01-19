package eu.clarin.cmdi.curation.cr;

import javax.xml.validation.Schema;

import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.pph.ProfileHeader;

public interface CRService {
	
	public ProfileHeader createProfileHeader(String schemaLocation, String cmdiVersion, boolean isLocalFile);

	ParsedProfile getParsedProfile(ProfileHeader header) throws NoProfileCacheEntryException;

	Schema getSchema(ProfileHeader header) throws NoProfileCacheEntryException;

	boolean isSchemaCRResident(String schemaLocation);
	
}
