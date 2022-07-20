package eu.clarin.cmdi.curation.cr.profile_parser;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;

import eu.clarin.cmdi.curation.configuration.CurationConfig;

public class TestBase {
   
   @Autowired
   private static CurationConfig conf;

   @BeforeAll
   public static void initClass() {

      if (conf.getDirectory().getXsdCache() == null) {
         File dir = new File(System.getProperty("java.io.tmpdir"), "private_profiles");
         dir.mkdir();

         conf.getDirectory().setXsdCache(dir.getParentFile().toPath());
      }
   }
}
