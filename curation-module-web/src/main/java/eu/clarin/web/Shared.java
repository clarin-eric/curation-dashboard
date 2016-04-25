package eu.clarin.web;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;
import eu.clarin.web.data.PublicProfile;

public class Shared {

	public static Path REPORTS_FOLDER = null; //make it configurable, or use from config
	
	public static List<CollectionReport> collections;	
	public static List<PublicProfile> publicProfiles;
	
	public static void init(){
		REPORTS_FOLDER = Configuration.OUTPUT_DIRECTORY.resolve("collections");
		initPublicProfiles();
		initCollections();
	}
	
	public static CollectionReport getCollectionReport(final String name){
		return collections.stream().filter(c -> c.fileReport.provider.equals(name)).findFirst().get();
	}
	
	private static void initPublicProfiles(){
		try {// .subList(0, 20)
			publicProfiles = CRService.getInstance().getPublicProfiles().parallelStream().map(p -> {
				try {
					CMDProfileReport report = (CMDProfileReport) new CurationModule().processCMDProfile(p.getId());
					return new PublicProfile(p.getId(), p.getName(), report.score, report.facet.profile.coverage, report.elements.percWithConcept);
				} catch (Exception e) {
					return new PublicProfile(p.getId(), p.getName(), -1, -1, -1);
				}
			}).collect(Collectors.toList());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void initCollections(){
		collections = new ArrayList<>();
		XMLMarshaller<CollectionReport> marshaller = new XMLMarshaller<>(CollectionReport.class);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(REPORTS_FOLDER)) {
			for (Path path : ds) {
				collections.add(marshaller.unmarshal(Files.newInputStream(path)));
			}
		} catch (Exception e) {
			System.out.println("Unable to process collection reports from" + REPORTS_FOLDER);
			e.printStackTrace();
		}
	}

}
