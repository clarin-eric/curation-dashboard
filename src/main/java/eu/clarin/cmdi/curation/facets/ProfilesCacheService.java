package eu.clarin.cmdi.curation.facets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDIProfile;

/**
 * @author dostojic
 *
 */
public class ProfilesCacheService {

    static final Logger _logger = LoggerFactory.getLogger(ProfilesCacheService.class);

    // singleton
    private static ProfilesCacheService instance = new ProfilesCacheService();

    private final Map<String, CMDIProfile> profilesCache = new ConcurrentHashMap<>();

    public static ProfilesCacheService getInstance() {
	return instance;
    }

    private ProfilesCacheService() {
    }
    
//    public CMDIProfile getCMDIProfile(String id){
//	if()
//	
//    }

}
