package eu.clarin.cmdi.curation.cr.profile_parser;
import org.junit.Before;
/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
import org.junit.Test;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSizeValidatorTest {
    CMDInstance entity; 
    
    @Before
    public void load() {
        Path path;
        try {
            Configuration.initDefault();
            
            path = Paths.get(getClass().getClassLoader().getResource("cmdi/cbmetadata_00024_cmdi.xml").toURI());

        
            entity = new CMDInstance(path, Files.size(path));
            
            CMDInstanceReport report = new CMDInstanceReport();
            
            FileSizeValidator fsv = new FileSizeValidator();
            
            fsv.process(entity, report);
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
    public void testFileSettings() {   

            
            assertTrue(entity.getPath().toString().endsWith("cmdi/cbmetadata_00024_cmdi.xml"));
            assertEquals(9925, entity.getSize());
            

            

    }
    
    @Test
    public void testParsedInstance() {
        
        assertNotNull(entity.getParsedInstance().getNodes());
        
        assertTrue(entity.getParsedInstance().getNodes().stream().anyMatch(node -> node.getXpath().equals("/cmd:CMD/@CMDVersion")));
        
    }
    
    @Test
    public void testCmdiData() {
        assertNotNull(entity.getCMDIData());
        
 
        
        assertEquals(1, entity.getCMDIData().getDataResources().size());
        
        assertEquals(1, entity.getCMDIData().getLandingPageResources().size());
        
        assertEquals(1, entity.getCMDIData().getSearchPageResources().size());
        
        assertEquals(0, entity.getCMDIData().getMetadataResources().size());
        
        assertEquals(1, entity.getCMDIData().getDocument().get("id").size());
        
        assertEquals("hdl_58_21.11114_47_COLL-0000-000B-CAB3-7", entity.getCMDIData().getDocument().get("id").get(0).getValue()); 

        
    }

}
