package eu.clarin.cmdi.curation.cr.profile_parser;

import org.junit.Test;
import static org.junit.Assert.*;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;


/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class ProfileParserTest extends TestBase{
       
    
    @Test 
    public void testParseProfile_1_2() throws Exception {
        
        ProfileHeader header = new ProfileHeader();
        
        header.id = "clarin.eu:cr1:p_1345561703620";
        header.cmdiVersion = "1.2";

        
        ParsedProfile profile = new CRService().getParsedProfile(header);
        
        profile.getXPaths().forEach(xpath -> System.out.println(xpath));
        
        assertEquals(true, header.isPublic);
        assertNotNull(profile.getXPaths());
    }
    


}
