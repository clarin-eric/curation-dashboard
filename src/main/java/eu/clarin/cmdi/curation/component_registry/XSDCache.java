package eu.clarin.cmdi.curation.component_registry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.io.Downloader;

public class XSDCache {
	
	private static final Logger _logger = LoggerFactory.getLogger(XSDCache.class);
	
	//Singleton
	private static XSDCache instance = new XSDCache();
	
	private Map<String, Schema> cache = new HashMap<>();
		
	private List<String> beingLoadedSchemas = new LinkedList<String>();
	
	private Map<String, Exception> exceptions = new HashMap<>();
	
	private XSDCache(){}
	
	public static XSDCache getInstance(){
		return instance;
	}
	
	
	public Schema getSchema(final String profile)throws Exception{		
		synchronized (cache) {			
			if(!cache.containsKey(profile)){
				if(!beingLoadedSchemas.contains(profile)){
					beingLoadedSchemas.add(profile);
					new Thread(){//if not in the cache and not being currently loaded start new Thread
						public void run() {
							try{
								Path pathToSchema = Paths.get(ComponentRegistryService.SCHEMA_FOLDER, profile + ".xsd");
								_logger.trace("Loading {} schema from {}", profile, pathToSchema);
								
								if(Files.notExists(pathToSchema)){
									_logger.trace("Schema for {} is not in the local FS, downloading it", profile);
									String profileURL = ComponentRegistryService.CLARIN_COMPONENT_REGISTRY_REST_URL + ComponentRegistryService.PROFILE_PREFIX + profile + "/xsd";
									Downloader downloader = new Downloader(profileURL, ComponentRegistryService.SCHEMA_FOLDER + "/" + profile + ".xsd");
									downloader.download();
								}
										
								SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);		
								Schema schema = schemaFactory.newSchema(pathToSchema.toFile());
								
								synchronized (cache) {
									cache.put(profile, schema);
									cache.notifyAll();
									beingLoadedSchemas.remove(profile);
								}
							
							}
							catch(Exception e){
								synchronized (cache) {
									cache.put(profile, null);
									cache.notifyAll();
									exceptions.put(profile, e);//save the exception because it goes into report
								}
							}
								
						}
					}.start();
				}
			}
				
			
			while(!cache.containsKey(profile))
				cache.wait();
				
		}
		
		Schema schema = cache.get(profile);
		if(schema == null)
			throw exceptions.get(profile);
		
		return schema;		
		
	}
		
	
}
