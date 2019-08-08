package eu.clarin.cmdi.curation.cr.profile_parser;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Report;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CurationModuleTest extends TestBase{

    @Before
    public void init() {
        super.init();
    }

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
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        assertFalse(Configuration.COLLECTION_MODE);
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
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        assertFalse(Configuration.COLLECTION_MODE);
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
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        assertFalse(Configuration.COLLECTION_MODE);
        assertTrue(report instanceof CMDInstanceReport);
    }
}
