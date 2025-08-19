package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.exception.NoCRCacheEntryException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;

import javax.xml.validation.Schema;
import java.util.stream.Stream;

public interface CRService {
	
	ProfileHeader createProfileHeader(String schemaLocation) throws CCRServiceNotAvailableException, PPHCacheException, NoCRCacheEntryException;

	ParsedProfile getParsedProfile(String schemaLocation) throws CCRServiceNotAvailableException, PPHCacheException, NoCRCacheEntryException;

	Schema getSchema(String schemaLocation) throws CCRServiceNotAvailableException, PPHCacheException, NoCRCacheEntryException;

	boolean isPublicSchema(String schemaLocation) throws PPHCacheException;

	Stream<String> getPublicSchemaLocations() throws PPHCacheException;

	String getIdFromSchemaLocation(String schemaLocation);
}
