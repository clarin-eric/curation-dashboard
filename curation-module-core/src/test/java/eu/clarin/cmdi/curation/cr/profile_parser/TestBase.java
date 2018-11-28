package eu.clarin.cmdi.curation.cr.profile_parser;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;

import eu.clarin.cmdi.curation.main.Configuration;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class TestBase {

    @Before
    public void init() {
        
        try {
            Configuration.initDefault();
            
            if(Configuration.CACHE_DIRECTORY == null) {
                File dir = new File(System.getProperty("java.io.tmpdir"), "private_profiles");
                dir.mkdir();
                
                Configuration.CACHE_DIRECTORY = dir.getParentFile().toPath();
            }
        }
        catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        
    }

}
