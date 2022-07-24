package eu.clarin.cmdi.curation.cr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.cr.conf.CRProperties;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest()
@Import(TestConfig.class)
@EnableConfigurationProperties
class CRServiceTests {
   
   @Autowired
   CRServiceFactory fac;
   
   @Autowired
   CRProperties crProps;

	@Test
	void getCRService() {
	   

	   
	   CRService service = fac.getCRService();
	   
	   PublicProfiles.createPublicProfiles(crProps.getCrRestUrl(), crProps.getCrQuery()).forEach(header -> {
	      try {
            service.getParsedProfile(header);
         }
         catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
	   });
	   

	}
}
