package eu.clarin.cmdi.curation.cr.profile_parser;


import java.io.IOException;

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
            
        }
        catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        
    }

}
