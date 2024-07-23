package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;

import javax.xml.validation.Schema;
import java.util.stream.Stream;

public interface CRService {
	
	public ProfileHeader createProfileHeader(String schemaLocation) throws NoProfileCacheEntryException, CRServiceStorageException, CCRServiceNotAvailableException;

	ParsedProfile getParsedProfile(String schemaLocation) throws NoProfileCacheEntryException, CCRServiceNotAvailableException, CRServiceStorageException;

	Schema getSchema(String schemaLocation) throws NoProfileCacheEntryException, CCRServiceNotAvailableException, CRServiceStorageException;

	boolean isPublicSchema(String schemaLocation);

	boolean isSchemaCRResident(String schemaLocation);

	Stream<String> getPublicSchemaLocations();

	String getIdFromSchemaLocation(String schemaLocation);
}
