package eu.clarin.cmdi.curation.ph;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.pph.PPHService;
import eu.clarin.cmdi.curation.pph.cache.PPHServiceImpl;
import eu.clarin.cmdi.curation.pph.conf.PHProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest()
@Import(TestConfig.class)
@EnableConfigurationProperties
@EnableAutoConfiguration
class ProfileDescriptionsTests {

   @Autowired
   PHProperties crProps;
   @Autowired
   PPHService phService;

   @Test
   void createPublicProfiles() {

      assertEquals(
            phService.getProfileHeaders().stream()
                  .filter(header -> header.getStatus().equals("deprecated")).count(),
            ((PPHServiceImpl) phService).getProfileHeaders(crProps.getCrRestUrl(), "?status=deprecated").size());

      assertEquals(
            phService.getProfileHeaders().stream()
                  .filter(header -> header.getStatus().equals("development")).count(),
                  ((PPHServiceImpl) phService).getProfileHeaders(crProps.getCrRestUrl(), "?status=development").size());

   }
}
