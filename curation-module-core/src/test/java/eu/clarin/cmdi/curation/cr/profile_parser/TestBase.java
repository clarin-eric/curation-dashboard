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
            

            Configuration.VLO_CONFIG.setFacetConceptsFile(this.getClass().getClassLoader().getResource("facetConcepts.xml").getFile());
        }
        catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        
    }

}
