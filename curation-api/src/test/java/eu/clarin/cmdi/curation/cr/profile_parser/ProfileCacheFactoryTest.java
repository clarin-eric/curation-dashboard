package eu.clarin.cmdi.curation.cr.profile_parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.cr.cache.CRServiceImpl;


public class ProfileCacheFactoryTest extends TestBase{

    
    @Test
    public void testParsedPublicProfile() {

        
        try {
            CRServiceImpl crService = new CRServiceImpl();
            
            ProfileHeader ph = new ProfileHeader();
            
            ph.setId("clarin.eu:cr1:p_1527668176046");
            ph.setSchemaLocation("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1527668176046/xsd");
            ph.setCmdiVersion("1.x");
            
            ParsedProfile pp = crService.getParsedProfile(ph);
            

            
            assertNotNull(pp.xpaths);
            
            assertFalse(pp.xpaths.isEmpty());
            
            assertTrue(ph.isPublic());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    //@Test
    public void testParsedPrivateProfile() {
        CRServiceImpl crService = new CRServiceImpl();
        
        ProfileHeader ph = new ProfileHeader();
        
        ph.setId("clarin.eu:cr1:p_1475136016193");
        ph.setSchemaLocation(" https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.1/profiles/clarin.eu:cr1:p_1475136016193/1.2/xsd");
        ph.setCmdiVersion("1.x");
        
        try {
            
            ParsedProfile pp = crService.getParsedProfile(ph);
            

            
            assertNotNull(pp.xpaths);
            
            assertFalse(pp.xpaths.isEmpty());
            
            assertFalse(ph.isPublic());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void initPublicProfiles(){

        
        try {
            List<ProfileHeader> profiles = (List<ProfileHeader>) new CRServiceImpl().getPublicProfiles();
            
            assertNotNull(profiles);
            
            assertTrue(profiles.size() > 0);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
