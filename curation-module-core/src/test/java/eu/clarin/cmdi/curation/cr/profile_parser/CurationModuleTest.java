package eu.clarin.cmdi.curation.cr.profile_parser;
/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
import org.junit.Test;


import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Report;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CurationModuleTest extends TestBase{
    Report<?> report; 
    
    
    
    @Test
    public void testIsCMDInstanceReport() { 
        Path path;
        
        try {
            path = Paths.get(getClass().getClassLoader().getResource("cmdi/cmdi-1_2.xml").toURI());

            CurationModule curation = new CurationModule();
            
            this.report = curation.processCMDInstance(path);
            this.report.toXML(System.out);
        }
        catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        assertTrue(this.report instanceof CMDInstanceReport);
    }
}
