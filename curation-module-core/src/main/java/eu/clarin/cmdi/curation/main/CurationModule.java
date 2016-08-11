package eu.clarin.cmdi.curation.main;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

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
		return new CMDProfile(CRService.CR_REST_1_2_PROFILES + profileId + "/xsd", "1.2").generateReport();
	}

	@Override
	public Report processCMDProfile(URL schemaLocation) {
		String cmdiVersion = "1.1";
		if(schemaLocation.toString().startsWith(CRService.CR_REST)){
			String version = schemaLocation.toString().substring(CRService.CR_REST.length(), CRService.CR_REST.length() + 3);
			if(version.startsWith("1."))
				cmdiVersion = version;
		}
		return new CMDProfile(schemaLocation.toString(), cmdiVersion).generateReport();
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
