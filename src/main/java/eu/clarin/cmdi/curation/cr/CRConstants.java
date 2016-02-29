/**
 * 
 */
package eu.clarin.cmdi.curation.cr;

/**
 * @author dostojic
 *
 */
public final class CRConstants {
    
    
    //move this to config
    public static final String REST_API = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/";
    public static final String PROFILE_PREFIX = "clarin.eu:cr1:";
    public static final String SCHEMA_FOLDER = "D:/xsd/";
    
    
    
    private CRConstants(){}
    
    public static String getProfilesURL(String profile){
	return REST_API + PROFILE_PREFIX + profile + "/";
    }

}
