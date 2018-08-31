package eu.clarin.web;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.facets.FacetConceptMappingService;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.report.CollectionReport;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;
import eu.clarin.web.data.PublicProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;

public class Shared {

	public static Path REPORTS_FOLDER = null; //make it configurable, or use from config
	
	public static List<CollectionReport> collections;
	public static List<PublicProfile> publicProfiles;
	public static Collection<String> facetNames;

	static final Logger _logger = LoggerFactory.getLogger(Shared.class);
	
	
	public static void init(){
		REPORTS_FOLDER = Configuration.OUTPUT_DIRECTORY.resolve("collections");
		//init facetNames
		facetNames = new FacetConceptMappingService().getFacetNames();		
		initPublicProfiles();
		initCollections();
		
		
	}
	
	public static CollectionReport getCollectionReport(final String name){
		return collections.stream().filter(c -> c.fileReport.provider.equals(name)).findFirst().get();
	}

	private static void initPublicProfiles(){
		try {
			List<ProfileHeader> profiles = (List<ProfileHeader>) new CRService().getPublicProfiles();
			publicProfiles = profiles.stream().map(p -> {//.subList(0, 10)
				Map<String, Boolean> facetMap = new LinkedHashMap<>();
				facetNames.forEach(name -> facetMap.put(name, false));				
				try {					
					CMDProfileReport report = (CMDProfileReport) new CurationModule().processCMDProfile(p.id);
					report.facet.coverage.stream().filter(f -> f.coveredByProfile).map(f -> f.name).forEach(f -> facetMap.put(f, true));					
					return new PublicProfile(p.id, p.name, report.score, report.facet.profileCoverage, report.elements.percWithConcept, facetMap);
				} catch (Exception e) {
					return new PublicProfile(p.id, p.name, -1, -1, -1, facetMap);
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

				try{
					collections.add(marshaller.unmarshal(Files.newInputStream(path)));
				}catch (JAXBException | NumberFormatException e) {
					_logger.error("Can't read from collection report: "+path+" :"+e.getMessage());
					//keep the for loop going to read the other collections
				}

			}
		} catch (IOException e) {
			_logger.error("Can't read the collections directory: "+e.getMessage());
		}
	}
}
