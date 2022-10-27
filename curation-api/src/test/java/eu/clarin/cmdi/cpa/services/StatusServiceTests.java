package eu.clarin.cmdi.cpa.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import eu.clarin.cmdi.cpa.model.Status;
import eu.clarin.cmdi.cpa.model.Url;
import eu.clarin.cmdi.cpa.repositories.RepositoryTests;
import eu.clarin.cmdi.cpa.service.StatusService;
import eu.clarin.cmdi.cpa.utils.Category;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

@SpringBootTest
class StatusServiceTests extends RepositoryTests{
  
   
   @Autowired
   private StatusService sService;

   @Transactional
	@Test
	void save() {
      
      Url url = uRep.save(new Url("http://www.wowasa.com", "www.wowasa.com", true));
      
      sService.save(new Status(url, Category.Broken, "", LocalDateTime.now()));
      
      assertEquals(1, sRep.count());
      assertEquals(0, hRep.count());
      
      //saving a record for same url copies old record to history
      sService.save(new Status(url, Category.Broken, "", LocalDateTime.now()));
      
      assertEquals(1, sRep.count());
      assertEquals(1, hRep.count());
      
	}

}
