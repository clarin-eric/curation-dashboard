package eu.clarin.cmdi.curation.cr.profile_parser;
import org.junit.Before;
/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
import org.junit.Test;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Report;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CurationModuleTest {
    Report<?> report; 
    
    @Before
    public void load() {
        
        Path path;
        try {
            
            Configuration.initDefault();
;
        
            path = Paths.get(getClass().getClassLoader().getResource("cmdi/cbmetadata_00024_cmdi.xml").toURI());
            //path = Paths.get(getClass().getClassLoader().getResource("cmdi/http_hdl_handle_net_11022_0000_0000_001B_2.xml").toURI());

            CurationModule curation = new CurationModule();
            
            this.report = curation.processCMDInstance(path);
        }

        catch (URISyntaxException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        
    }
    
    @Test
    public void testIsCMDInstanceReport() { 
        
        try {
            this.report.toXML(System.out);
        }
        catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        assertTrue(this.report instanceof CMDInstanceReport);
    }
}
