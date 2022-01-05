package eu.clarin.cmdi.curation.cr.profile_parser;

import eu.clarin.cmdi.curation.main.Configuration;

import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;



public class TestBase {

    @BeforeClass
    public static void initClass() {

        try {

            Configuration.initDefault();
 
            if (Configuration.CACHE_DIRECTORY == null) {
                File dir = new File(System.getProperty("java.io.tmpdir"), "private_profiles");
                dir.mkdir();

                Configuration.CACHE_DIRECTORY = dir.getParentFile().toPath();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
