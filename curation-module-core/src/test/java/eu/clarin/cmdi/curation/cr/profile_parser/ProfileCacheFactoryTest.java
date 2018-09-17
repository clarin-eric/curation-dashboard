package eu.clarin.cmdi.curation.cr.profile_parser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
import org.junit.*;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.main.Configuration;

public class ProfileCacheFactoryTest {
    
    @Test
    public void testProfileCachEntry() {
        CRService crService = new CRService();
        
        ProfileHeader ph = new ProfileHeader();
        
        ph.schemaLocation = "https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.2/profiles/clarin.eu:cr1:p_1361876010587";
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

}
