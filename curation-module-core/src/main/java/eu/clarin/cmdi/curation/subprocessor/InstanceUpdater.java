package eu.clarin.cmdi.curation.subprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.instance_parser.InstanceParser;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.TransformationReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class InstanceUpdater extends CMDSubprocessor {
    private final static Logger _logger = LoggerFactory.getLogger(InstanceUpdater.class);
    private static final Pattern _pattern = Pattern.compile("xmlns(:.+?)?=\"http(s)?://www.clarin.eu/cmd/(1)?");
    
    private static Transformer _transformer = null;
    
    static{
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(InstanceParser.class.getResourceAsStream("/cmd-record-1_1-to-1_2.xsl"));       
        try {
            _transformer = factory.newTransformer(xslt);
        } 
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Unable to open cmd-record-1_1-to-1_2.xsl", e);
        }
    }

    @Override
    public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
        report.transformationReport = new TransformationReport();
        
        Path newPath = Files.createTempFile(null, null);
        if(!isLatestVersion(entity.getPath())){
            _transformer.transform(new StreamSource(entity.getPath().toFile()), new StreamResult(newPath.toFile()));
            
            this.addMessage(Severity.INFO, "tranformed cmdi version 1.1 into version 1.2");
            
            entity.setPath(newPath);
            entity.setSize(Files.size(newPath));
        }

    }

    @Override
    public Score calculateScore(CMDInstanceReport report) {
        
        return new Score(0.0, 0.0, "transformation-section", this.msgs);
    }
    
    private boolean isLatestVersion(Path path) throws IOException {
        String line = null;
        Matcher matcher;
        
        try(BufferedReader reader = Files.newBufferedReader(path)){
            
            while((line = reader.readLine()) != null) 
                if((matcher = _pattern.matcher(line)).find())
                    return matcher.group(3) != null;
        }
        catch(IOException ex) {
                
        }


        
        return false;
    }

}
