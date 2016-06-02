package eu.clarin.cmdi.curation.main;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Matcher;

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
	public Report processCMDProfile(String profileId) {
		return new CMDProfile(profileId).generateReport();
	}

	@Override
	public Report processCMDProfile(URL url) {
		Matcher m = CRService.PROFILE_ID_PATTERN.matcher(url.toString());
		if(m.find()){
			return new CMDProfile(m.group()).generateReport();
		}
		throw new IllegalArgumentException(url + " must contain profileID");
	}

	@Override
	public Report processCMDInstance(Path file) throws IOException {
		if (Files.notExists(file))
			throw new IOException(file.toString() + " doesn't exist!");
		return new CMDInstance(file, Files.size(file)).generateReport();
	}
	

	@Override
	public Report processCMDInstance(URL url) throws IOException{		
		Path path = Files.createTempFile(null, null);		
		new Downloader().download(url.toString(), path.toFile());
		long size = Files.size(path);
		Report r = new CMDInstance(path, size).generateReport();
		Files.delete(path);
		
		((CMDInstanceReport)r).fileReport.location = url.toString();
		return r;
	}

	@Override
	public Report processCollection(Path path) throws IOException {
		CMDFileVisitor entityTree = new CMDFileVisitor();
		Files.walkFileTree(path, entityTree);
		CMDCollection collection = entityTree.getRoot();
		
		return collection.generateReport();
	}

	@Override
	public Report aggregateReports(Collection<Report> reports) {
		// TODO Auto-generated method stub
		return null;
	}
}
