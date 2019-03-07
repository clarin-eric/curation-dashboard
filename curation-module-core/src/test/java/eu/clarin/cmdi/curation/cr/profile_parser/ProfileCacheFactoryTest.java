package eu.clarin.cmdi.curation.cr.profile_parser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import java.util.List;


import org.junit.*;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.main.Configuration;


public class ProfileCacheFactoryTest extends TestBase{

    
    @Test
    public void testParsedPublicProfile() {

        
        try {
            
            
            CRService crService = new CRService();
            
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
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
    
    //@Test
    public void testParsedPrivateProfile() {
        CRService crService = new CRService();
        
        ProfileHeader ph = new ProfileHeader();
        
        ph.setId("clarin.eu:cr1:p_1475136016193");
        ph.setSchemaLocation(" https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.1/profiles/clarin.eu:cr1:p_1475136016193/1.2/xsd");
        ph.setCmdiVersion("1.x");
        
        try {
            Configuration.initDefault();
            
            ParsedProfile pp = crService.getParsedProfile(ph);
            

            
            assertNotNull(pp.xpaths);
            
            assertFalse(pp.xpaths.isEmpty());
            
            assertFalse(ph.isPublic());
        }
        catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
    
    @Test
    public void initPublicProfiles(){

        
        try {
            List<ProfileHeader> profiles = (List<ProfileHeader>) new CRService().getPublicProfiles();
            
            assertNotNull(profiles);
            
            assertTrue(profiles.size() > 0);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
