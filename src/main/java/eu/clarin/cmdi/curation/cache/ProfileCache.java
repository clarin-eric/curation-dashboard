/**
 * 
 */
package eu.clarin.cmdi.curation.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDIProfile;

/**
 * @author dostojic
 *
 */
public class ProfileCache extends AbstractCache<String, CMDIProfile> {
    //private static final Logger _logger = LoggerFactory.getLogger(ProfileCache.class);

    // Singleton
    private static ProfileCache instance = new ProfileCache();

    private ProfileCache() {
    }

    public static ProfileCache getInstance() {
	return instance;
    }

    @Override
    public CMDIProfile lookup(String key) throws Exception {
	// TODO Auto-generated method stub
	return null;
    }

}
