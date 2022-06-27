package eu.clarin.cmdi.curation.cr.profile_parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;

public class ProfileParserTest extends TestBase{
       
    
    @Test 
    public void testParseProfile_1_2() throws Exception {
        
        ProfileHeader header = new ProfileHeader();
        
        header.setId("clarin.eu:cr1:p_1493735943947");
        header.setSchemaLocation("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.1/profiles/clarin.eu:cr1:p_1493735943947/1.2/xsd");
        header.setCmdiVersion("1.2");

        
        ParsedProfile profile = new CRService().getParsedProfile(header);
        
        profile.getXPaths().forEach(xpath -> System.out.println(xpath));
        
        assertEquals(true, header.isPublic());
        assertNotNull(profile.getXPaths());
    }
    


}
