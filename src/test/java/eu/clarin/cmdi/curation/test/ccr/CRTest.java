package eu.clarin.cmdi.curation.test.ccr;

import java.util.Map.Entry;

import eu.clarin.cmdi.curation.cr.CRService;

public class CRTest {

	
	public static void main(String[] args) throws Exception {
		CRService crService = CRService.getInstance();
		for (Entry<String, String> profile : crService.getPublicProfiles().entrySet()) {
			System.out.println(profile.getKey() + ": " + profile.getValue());
		}
	}

}
