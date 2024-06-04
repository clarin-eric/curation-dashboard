package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.pph.exception.PPHServiceNotAvailableException;

import javax.xml.validation.Schema;

public interface CRService {
	
	public ProfileHeader createProfileHeader(String schemaLocation, String cmdiVersion, boolean isLocalFile) throws PPHServiceNotAvailableException;

	ParsedProfile getParsedProfile(ProfileHeader header) throws NoProfileCacheEntryException, CRServiceStorageException, PPHServiceNotAvailableException, CCRServiceNotAvailableException;

	Schema getSchema(ProfileHeader header) throws NoProfileCacheEntryException, CRServiceStorageException, PPHServiceNotAvailableException, CCRServiceNotAvailableException;

	boolean isSchemaCRResident(String schemaLocation);
	
}
