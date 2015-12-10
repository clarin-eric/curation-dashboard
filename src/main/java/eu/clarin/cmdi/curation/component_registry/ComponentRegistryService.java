package eu.clarin.cmdi.curation.component_registry;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.clarin.cmdi.curation.component_registry.CMDIProfiles.ProfileDescription;
import eu.clarin.cmdi.curation.io.Downloader;

public class ComponentRegistryService {
	
	private static final Logger _logger = LoggerFactory.getLogger(ComponentRegistryService.class);
	
	public static final String CLARIN_COMPONENT_REGISTRY_REST_URL = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/";
	public static final String PROFILE_PREFIX = "clarin.eu:cr1:p_";
	public static final String SCHEMA_FOLDER = "D:/xsd";
	
	private CMDIProfiles profiles = null;
	
	private static volatile  ComponentRegistryService instance = new ComponentRegistryService();
		
	private ComponentRegistryService(){
		//create xsd dir if it doesnt exist
		File xsdDir = new File(SCHEMA_FOLDER);
		xsdDir.mkdirs();
		
//		if(clearCache)
//			clearCache
		
	}
	
	public static ComponentRegistryService getInstance(){
		return instance;
	}
	
	public CMDIProfiles getProfilesFromComponentRegistry() throws Exception{
		if(profiles == null){
			profiles = (CMDIProfiles) unmarshal(new URL(CLARIN_COMPONENT_REGISTRY_REST_URL).openStream(), CMDIProfiles.class);
		}
		
		return profiles;
	}
	
	public List<String> getProfileIDs() throws Exception{
		CMDIProfiles profiles = getProfilesFromComponentRegistry();
		return profiles
				.getProfiles()
				.stream()
				.map(ProfileDescription::getId)
				.map(profileID -> profileID.substring(PROFILE_PREFIX.length())) //strip prefix
				.collect(Collectors.toList());
	}
		
	
	 public void downloadSchemas(List<String> profileIds) throws Exception{		
		try{
			List<Downloader> tasks = profileIds
					.stream()
					.map(profile -> new Downloader(getProfileURL(profile), SCHEMA_FOLDER + "/" + profile + ".xsd"))
					.collect(Collectors.toList());
			
			ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			_logger.trace("Started downloading profile XSDs");
			long start = System.currentTimeMillis();
			executor.invokeAll(tasks)
				.stream()
				.forEach(future -> {
					try{
						future.get();
					}catch(Exception e){
						throw new RuntimeException(e);
					}
				});
			_logger.trace("Download lasted {}ms", System.currentTimeMillis() - start);
			executor.shutdown();
		}catch(Exception e){
			_logger.error("Unable to download profile XSDs");
			throw new RuntimeException(e);
		}
	}

			
	private Object unmarshal(InputStream in, Class clazz) throws Exception{
	    JAXBContext jc = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return unmarshaller.unmarshal(in);
	}
	
	
	public String getProfileURL(String profileId) {
		return CLARIN_COMPONENT_REGISTRY_REST_URL + PROFILE_PREFIX + profileId + "/xsd";
	}
	
	public static void main(String[] args) {	
		
		ComponentRegistryService crs = ComponentRegistryService.getInstance();
		try {
			crs.downloadSchemas(Lists.newArrayList("clarin.eu:cr1:p_1357720977520"));
			//crs.downloadSchemas(crs.getProfileIDs());
		} catch (Exception e) {
			System.out.println("unable to download xsd schemas");
			e.printStackTrace();
		}
	}

}
