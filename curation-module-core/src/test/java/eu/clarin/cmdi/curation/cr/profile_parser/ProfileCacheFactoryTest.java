package eu.clarin.cmdi.curation.cr.profile_parser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
import org.junit.*;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDProfileReport;

public class ProfileCacheFactoryTest {
    
    @Test
    public void testParsedPublicProfile() {
        CRService crService = new CRService();
        
        ProfileHeader ph = new ProfileHeader();
        
        ph.id = "clarin.eu:cr1:p_1361876010587";
        ph.cmdiVersion = "1.2";
        
        try {
            Configuration.initDefault();
            
            ParsedProfile pp = crService.getParsedProfile(ph);
            

            
            assertNotNull(pp.xpaths);
            
            assertFalse(pp.xpaths.isEmpty());
            
            assertTrue(ph.isPublic);
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
        
        ph.id = "clarin.eu:cr1:p_1493735943947";
        ph.cmdiVersion = "1.2";
        
        try {
            Configuration.initDefault();
            
            ParsedProfile pp = crService.getParsedProfile(ph);
            

            
            assertNotNull(pp.xpaths);
            
            assertFalse(pp.xpaths.isEmpty());
            
            assertTrue(ph.isPublic);
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
