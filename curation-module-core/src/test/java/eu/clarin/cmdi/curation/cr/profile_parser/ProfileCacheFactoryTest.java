package eu.clarin.cmdi.curation.cr.profile_parser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import java.util.List;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
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
            
            ph.setId("clarin.eu:cr1:p_1361876010587");
            ph.setCmdiVersion("1.2");
            
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
        
        ph.setId("clarin.eu:cr1:p_1493735943947");
        ph.setCmdiVersion("1.2");
        
        try {
            Configuration.initDefault();
            
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
