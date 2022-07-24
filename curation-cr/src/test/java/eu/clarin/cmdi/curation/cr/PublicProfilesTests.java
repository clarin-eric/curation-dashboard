package eu.clarin.cmdi.curation.cr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.cr.conf.CRProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;

@SpringBootTest()
@Import(TestConfig.class)
@EnableConfigurationProperties
class PublicProfilesTests {
   
   @Autowired
   CRProperties crProps;

	@Test
	void createPublicProfiles() {
	   
	   Collection<ProfileHeader> publicProfiles = PublicProfiles.createPublicProfiles(crProps.getCrRestUrl(), crProps.getCrQuery());
	   
	   
	   assertEquals(
	         publicProfiles.stream().filter(header -> header.getStatus().equalsIgnoreCase("deprecated")).count(), 
	         PublicProfiles.createPublicProfiles(crProps.getCrRestUrl(), "?status=deprecated").size()
         );
	     assertEquals(
	            publicProfiles.stream().filter(header -> header.getStatus().equalsIgnoreCase("development")).count(), 
	            PublicProfiles.createPublicProfiles(crProps.getCrRestUrl(), "?status=development").size()
	         );  

	}
}
