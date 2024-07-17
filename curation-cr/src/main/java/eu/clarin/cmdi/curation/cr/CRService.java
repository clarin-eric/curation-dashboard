package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;

import javax.xml.validation.Schema;
import java.util.stream.Stream;

public interface CRService {
	
	public ProfileHeader createProfileHeader(String schemaLocation, boolean isCacheable) throws NoProfileCacheEntryException, CRServiceStorageException, CCRServiceNotAvailableException;

	ParsedProfile getParsedProfile(String schemaLocation, boolean isCacheable) throws NoProfileCacheEntryException, CCRServiceNotAvailableException, CRServiceStorageException;

	Schema getSchema(String schemaLocation, boolean isCacheable) throws NoProfileCacheEntryException, CCRServiceNotAvailableException, CRServiceStorageException;

	boolean isSchemaCRResident(String schemaLocation);

	Stream<String> getPublicSchemaLocations();
}
