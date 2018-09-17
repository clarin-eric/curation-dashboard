package eu.clarin.cmdi.curation.cr.profile_parser;
import org.junit.Before;
/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
import org.junit.Test;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;
import eu.clarin.cmdi.curation.subprocessor.InstanceHeaderProcessor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InstanceHeaderProcessorTest {
    CMDInstance entity; 
    CMDInstanceReport report;
    
    @Before
    public void load() {
        Path path;
        try {
            path = Paths.get(getClass().getClassLoader().getResource("cmdi/cbmetadata_00024_cmdi.xml").toURI());

        
            entity = new CMDInstance(path, Files.size(path));
            
            report = new CMDInstanceReport();
            
            FileSizeValidator fsv = new FileSizeValidator();
            
            fsv.process(entity, report);
            
            new InstanceHeaderProcessor().process(entity, report);
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
    public void testHeader() {
        
          
        assertEquals("clarin.eu:cr1:p_1493735943947", this.report.header.id);

        assertEquals("1.2", this.report.header.cmdiVersion);
        assertEquals("CollBank", this.report.fileReport.collection); 
        


    }

}
