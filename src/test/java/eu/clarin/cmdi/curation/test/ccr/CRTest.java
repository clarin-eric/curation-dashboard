package eu.clarin.cmdi.curation.test.ccr;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CRTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void test() throws Exception {
//		CRService crService = CRService.getInstance();
//		CMDIProfiles profiles = crService.getProfilesFromComponentRegistry();
		
//		Assert.assertNotEquals("number of returned profiles must be around 200", 0, profiles.count());
//		
//		String annotationToolProfileID  = "clarin.eu:cr1:p_1297242111880";
//		Assert.assertEquals("Returned url doesnt match the expected one for profile ID " + annotationToolProfileID, 
//				"http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1297242111880/xsd",
//				profiles.getXsdUrl(annotationToolProfileID));
//		Assert.assertNull("XSD for random string must be null", profiles.getXsdUrl("blah blah"));
		
	}

}
