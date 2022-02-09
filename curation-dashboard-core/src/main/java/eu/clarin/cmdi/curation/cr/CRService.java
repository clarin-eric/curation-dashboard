package eu.clarin.cmdi.curation.cr;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;

import eu.clarin.cmdi.curation.cr.ProfileCacheFactory.ProfileCacheEntry;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.main.Configuration;

public class CRService implements ICRService {
	static final Logger LOG = LoggerFactory.getLogger(CRService.class);
	
	//https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles?registrySpace=published&status=*
	//https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles?registrySpace=published&status=production
	//https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles?registrySpace=published&status=development
	
	public static final String CR_REST = Configuration.VLO_CONFIG.getComponentRegistryRESTURL().replaceFirst("http(s)?", "(http|https)").replaceFirst("/1\\..+", "/.+");
	public static final Pattern CR_REST_PATTERN = Pattern.compile(CR_REST);
	
	public static final String PROFILE_PREFIX = "clarin.eu:cr1:";
	public static final String PROFILE_ID_FORMAT = "clarin\\.eu:cr1:p_[0-9]+";
	public static final Pattern PROFILE_ID_PATTERN = Pattern.compile(PROFILE_ID_FORMAT);

	private static final Collection<ProfileHeader> publicProfiles = PublicProfiles.createPublicProfiles();
	public static Double PROFILE_MAX_SCORE = Double.NaN;
	
	
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
		if(!isLocalFile && schemaLocation.startsWith(Configuration.VLO_CONFIG.getComponentRegistryRESTURL()))
			header = publicProfiles
			.stream()
			.filter(h -> schemaLocation.contains(h.getId()))
			.findFirst()
			.orElse(null);
		
		if(header == null){
			header = new ProfileHeader();
			header.setSchemaLocation(schemaLocation);
			header.setId(getIdFromSchemaLocation(schemaLocation));
	        header.setCmdiVersion(cmdiVersion);
	        header.setPublic(false);
	        
			if(header.getId() == null) { // when the id can't be extracted from the schema location we have to get it from the file content
		        CharBuffer buffer = CharBuffer.allocate(1000);
		        
		        InputStreamReader reader;
                try {
                    reader = new InputStreamReader(new URL(schemaLocation).openStream());
                    reader.read(buffer);
                    String content = buffer.rewind().toString();
                    
                    Matcher matcher = PROFILE_ID_PATTERN.matcher(content);
                    
                    if(matcher.find())
                        header.setId(matcher.group());
                    
                    if(!content.contains("http://www.clarin.eu/cmd/1"))
                        header.setCmdiVersion("1.1");
                }
                catch (MalformedURLException ex) {
                    LOG.error("Schema location "  + schemaLocation + " is no valid URL.");
                }
                catch (IOException ex) {
                    LOG.error("Couldn't read from schema location: " + schemaLocation);
                }  
			}
		}
		header.setLocalFile(isLocalFile);		
		return header;		
	}
	

	@Override
	public ParsedProfile getParsedProfile(ProfileHeader header) throws ExecutionException {
		//logger.debug("parsed profile lookup for {} from cache", header);
		boolean isPublic = header.isPublic();
		boolean isNewest = isTheNewestCMDIVersion(header.getCmdiVersion());

		LoadingCache<ProfileHeader, ProfileCacheEntry> cache;
		if(isPublic && isNewest){
			cache = publicProfilesCache;
		}else{
			cache = nonpublicProfilesCache;
		}

		ProfileCacheEntry profileCacheEntry = cache.get(header);

		return profileCacheEntry.parsedProfile;
	}
	
	public boolean isTheNewestCMDIVersion(String cmdVersion){
		return cmdVersion.equals("1.x") || cmdVersion.equals("1.2");
	}
	
	@Override
	public Schema getSchema(ProfileHeader header) throws ExecutionException {
		return (header.isPublic()? publicProfilesCache : nonpublicProfilesCache).get(header).schema;
	}
	
	public double getScore(ProfileHeader header) throws ExecutionException {
		return (header.isPublic()? publicScoreCache : nonpublicScoreCache).get(header);
	}

	@Override
	public boolean isNameUnique(String name){
		return publicProfiles.stream().filter(h -> h.getName().equals(name)).count() <= 1;
	}

	@Override
	public boolean isDescriptionUnique(String description){
		return publicProfiles.stream().filter(h -> h.getName().equals(description)).count() <= 1;
	}

	@Override
	public boolean isSchemaCRResident(String schemaLocation) {
		return CR_REST_PATTERN.matcher(schemaLocation).matches();
	}

	@Override
	public Collection<ProfileHeader> getPublicProfiles(){
		return publicProfiles;
	}
	
	public String getIdFromSchemaLocation(String schemaLocation) {
	    Matcher matcher = PROFILE_ID_PATTERN.matcher(schemaLocation);
	    
	    return matcher.find()? matcher.group():null;
	}
}
