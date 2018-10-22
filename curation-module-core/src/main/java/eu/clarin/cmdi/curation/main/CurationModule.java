package eu.clarin.cmdi.curation.main;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.instance_parser.InstanceParser;
import eu.clarin.cmdi.curation.io.CMDFileVisitor;
import eu.clarin.cmdi.curation.io.Downloader;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Report;

public class CurationModule implements CurationModuleInterface {
    Pattern pattern = Pattern.compile("CMDVersion=\"(\\d{1}\\..{1})\"");
    
    private static Transformer _transformer = null;
    
    static{
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(InstanceParser.class.getResourceAsStream("/cmd-record-1_1-to-1_2.xsl"));       
        try {
            _transformer = factory.newTransformer(xslt);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Unable to open cmd-record-1_1-to-1_2.xsl", e);
        }
    }

	@Override
	public Report<?> processCMDProfile(String profileId) throws InterruptedException {
	    return new CMDProfile(profileId, "1.x").generateReport();
	}

	@Override
	public Report<?> processCMDProfile(URL schemaLocation) throws InterruptedException {
	    Matcher matcher; 
	    if((matcher = CRService.PROFILE_ID_PATTERN.matcher(schemaLocation.toString())).find())
	        return new CMDProfile(matcher.group(0), "1.x").generateReport();
	    
	    return null;
/*		String cmdiVersion = "1.1";
		if(schemaLocation.toString().startsWith(Configuration.vloConfig.getComponentRegistryRESTURL())){
			String version = schemaLocation.toString().substring(Configuration.vloConfig.getComponentRegistryRESTURL().length(), Configuration.vloConfig.getComponentRegistryRESTURL().length() + 3);
			if(version.startsWith("1."))
				cmdiVersion = version;
		}
		return new CMDProfile(schemaLocation.toString(), cmdiVersion).generateReport();*/
	}

	@Override
	public Report<?> processCMDInstance(Path path) throws IOException, InterruptedException, TransformerException {
		if (Files.notExists(path))
			throw new IOException(path.toString() + " doesn't exist!");		
		
		return new CMDInstance(path, Files.size(path)).generateReport();
	}
	

	@Override
	public Report<?> processCMDInstance(URL url) throws IOException, InterruptedException {
		Path path = Files.createTempFile(null, null);
		new Downloader().download(url.toString(), path.toFile());
		long size = Files.size(path);
		CMDInstance cmdInstance = new CMDInstance(path, size);
		cmdInstance.setUrl(url.toString());
		Report<?> r = cmdInstance.generateReport();
		Files.delete(path);

		if(r instanceof CMDInstanceReport){
			((CMDInstanceReport)r).fileReport.location = url.toString();
		}

		return r;
	}

	@Override
	public Report<?> processCollection(Path path) throws IOException, InterruptedException {
		CMDFileVisitor entityTree = new CMDFileVisitor();
		Files.walkFileTree(path, entityTree);
		CMDCollection collection = entityTree.getRoot();

		return collection.generateReport();
	}

	@Override
	public Report<?> aggregateReports(Collection<Report> reports) {
		// TODO Auto-generated method stub
		return null;
	}

}
