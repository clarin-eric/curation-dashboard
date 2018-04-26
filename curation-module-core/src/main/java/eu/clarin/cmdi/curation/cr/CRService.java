package eu.clarin.cmdi.curation.cr;

import java.util.Collection;
import java.util.regex.Pattern;

import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;

import eu.clarin.cmdi.curation.cr.ProfileCacheFactory.ProfileCacheEntry;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;

public class CRService implements ICRService {
	static final Logger logger = LoggerFactory.getLogger(CRService.class);
	
	public static final String CR_REST = "https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/";
	public static final String CR_REST_1_2_PROFILES = CR_REST + "1.2/profiles/";
	public static final String PROFILE_PREFIX = "clarin.eu:cr1:";
	public static final String PROFILE_ID_FORMAT = "clarin\\.eu:cr1:p_[0-9]+";
	public static final Pattern PROFILE_ID_PATTERN = Pattern.compile(PROFILE_ID_FORMAT);

	private static Collection<ProfileHeader> publicProfiles = PublicProfiles.createPublicProfiles();
	public static Double PROFILE_MAX_SCORE = new Double(Double.NaN);
	
	
	//cache for parsedProfile and schema obj
	private static final LoadingCache<ProfileHeader, ProfileCacheEntry> publicProfilesCache = ProfileCacheFactory.createProfileCache(true);
	private static final LoadingCache<ProfileHeader, ProfileCacheEntry> nonpublicProfilesCache = ProfileCacheFactory.createProfileCache(false);
	
	//cache for score
	private static final LoadingCache<ProfileHeader, Double> publicScoreCache = ProfileScoreCacheFactory.createScoreCache(true);
	private static final LoadingCache<ProfileHeader, Double> nonpublicScoreCache = ProfileScoreCacheFactory.createScoreCache(false);

	
	/*
	 * Profile is considered to be public if schema resides in Component Registry
	 * and the ID is in the list of public profiles  
	 */
	
	@Override
	public ProfileHeader createProfileHeader(String schemaLocation, String cmdiVersion, boolean isLocalFile){		
		ProfileHeader header = null;
		if(!isLocalFile || isSchemaCRResident(schemaLocation))
			header = publicProfiles
			.stream()
			.filter(h -> h.schemaLocation.equals(schemaLocation))
			.findFirst()
			.orElse(null);
		
		if(header == null){
			header = new ProfileHeader();
			header.schemaLocation = schemaLocation;
			header.cmdiVersion = cmdiVersion;
			header.isPublic = false;			
		}
		header.isLocalFile = isLocalFile;		
		return header;		
	}
	

	@Override
	public ParsedProfile getParsedProfile(ProfileHeader header) throws Exception{
		//logger.debug("parsed profile lookup for {} from cache", header);
		return (header.isPublic && isTheNewestCMDIVersion(header.cmdiVersion) ? publicProfilesCache : nonpublicProfilesCache).get(header).parsedProfile;		
	}
	
	public boolean isTheNewestCMDIVersion(String cmdVersion){
		return cmdVersion.equals("1.x") || cmdVersion.equals("1.2");
	}
	
	@Override
	public Schema getSchema(ProfileHeader header) throws Exception{
		//logger.debug("schema lookup for {} from cache", header);
		return (header.isPublic? publicProfilesCache : nonpublicProfilesCache).get(header).schema;		
	}
	
	public double getScore(ProfileHeader header) throws Exception{
		//logger.debug("score lookup for {} from cache", header);
		return (header.isPublic? publicScoreCache : nonpublicScoreCache).get(header);
	}

	@Override
	public boolean isNameUnique(String name){
		return publicProfiles.stream().filter(h -> h.name.equals(name)).count() <= 1;
	}

	@Override
	public boolean isDescriptionUnique(String description){
		return publicProfiles.stream().filter(h -> h.name.equals(description)).count() <= 1;
	}

	@Override
	public boolean isSchemaCRResident(String schemaLocation) {
		return schemaLocation.startsWith(CR_REST);
	}

	@Override
	public Collection<ProfileHeader> getPublicProfiles(){
		return publicProfiles;
	}

}
