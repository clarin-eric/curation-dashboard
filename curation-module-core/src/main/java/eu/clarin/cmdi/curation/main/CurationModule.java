package eu.clarin.cmdi.curation.main;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Matcher;

import javax.xml.transform.TransformerException;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.io.CMDFileVisitor;
import eu.clarin.cmdi.curation.io.Downloader;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.Report;

public class CurationModule implements CurationModuleInterface {    

	@Override
	public Report<?> processCMDProfile(String profileId) throws InterruptedException {
	    return new CMDProfile(profileId, "1.x").generateReport(null);

	}

	@Override
	public Report<?> processCMDProfile(URL schemaLocation) throws InterruptedException {
	    Matcher matcher; 
	    if((matcher = CRService.PROFILE_ID_PATTERN.matcher(schemaLocation.toString())).find())
	        return new CMDProfile(matcher.group(0), "1.x").generateReport(null);
	    
	    return null;

	}

	@Override

	public Report<?> processCMDInstance(Path path) throws IOException, InterruptedException, TransformerException {
		if (Files.notExists(path))
			throw new IOException(path.toString() + " doesn't exist!");		
		
		return new CMDInstance(path, Files.size(path)).generateReport(null);

	}
	

	@Override
	public Report<?> processCMDInstance(URL url) throws IOException, InterruptedException {
		Path path = Files.createTempFile(null, null);
		new Downloader().download(url.toString(), path.toFile());
		long size = Files.size(path);
		CMDInstance cmdInstance = new CMDInstance(path, size);
		cmdInstance.setUrl(url.toString());

		Report<?> r = cmdInstance.generateReport(null);

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

		return collection.generateReport(null);
	}

	@Override
	public Report<?> aggregateReports(Collection<Report> reports) {
		// TODO Auto-generated method stub
		return null;
	}

}
