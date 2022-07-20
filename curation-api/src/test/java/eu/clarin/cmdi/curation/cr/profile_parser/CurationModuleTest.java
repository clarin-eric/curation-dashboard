package eu.clarin.cmdi.curation.cr.profile_parser;

import eu.clarin.cmdi.curation.configuration.CurationConfig;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Report;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CurationModuleTest extends TestBase{
   
   @Autowired
   private CurationConfig conf; 

    @Test
    public void testCMD_1_1() { 
        Report<?> report = null; 
        Path path;
        
        try {
            path = Paths.get(getClass().getClassLoader().getResource("cmdi/cmdi-1_1.xml").toURI());

            CurationModule curation = new CurationModule();
            
            report = curation.processCMDInstance(path);
        }
        catch (Exception ex) {

            ex.printStackTrace();
        }
        assertFalse(conf.getMode().equalsIgnoreCase("collection"));
        assertTrue(report instanceof CMDInstanceReport);        

    }    
    
    
    @Test
    public void testCMD_1_2() { 
        Report<?> report = null; 
        Path path;
        
        try {
            path = Paths.get(getClass().getClassLoader().getResource("cmdi/cmdi-1_2.xml").toURI());

            CurationModule curation = new CurationModule();
            
            report = curation.processCMDInstance(path);
        }
        catch (Exception ex) {

            ex.printStackTrace();
        }
        assertFalse(conf.getMode().equalsIgnoreCase("collection"));
        assertTrue(report instanceof CMDInstanceReport);
    }
    
    @Test
    public void testCMD_1_x() { 
        Report<?> report = null; 
        Path path;
        
        try {
            path = Paths.get(getClass().getClassLoader().getResource("cmdi/cmdi-1_x.xml").toURI());

            CurationModule curation = new CurationModule();
            
            report = curation.processCMDInstance(path);
        }
        catch (Exception ex) {

            ex.printStackTrace();
        }
        assertFalse(conf.getMode().equalsIgnoreCase("collection"));
        assertTrue(report instanceof CMDInstanceReport);
    }
}
