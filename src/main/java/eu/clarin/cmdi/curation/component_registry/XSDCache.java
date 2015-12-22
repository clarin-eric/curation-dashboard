package eu.clarin.cmdi.curation.component_registry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.io.Downloader;
import eu.clarin.cmdi.curation.xml.CMDIContentHandler;

public class XSDCache {
	
	private static final Logger _logger = LoggerFactory.getLogger(XSDCache.class);
	
	//Singleton
	private static volatile  XSDCache instance = new XSDCache();
	
	private Map<String, Schema> cache = new HashMap<>();
	
	private XSDCache(){}
	
	public static XSDCache getInstance(){
		return instance;
	}
		
	
	public Schema getSchema(String profile) throws Exception{
		if(!cache.containsKey(profile)){
			//fall-back: xsd dir, download
			_logger.trace("Schema for {} is not in the cache, searching in the local FS", profile);
			Schema schema = loadSchemaFromFile(profile);
			cache.put(profile, schema);
		}		
		
		return cache.get(profile);
	}
	
	private Schema loadSchemaFromFile(String profile) throws Exception{		
		Path schema = Paths.get(ComponentRegistryService.SCHEMA_FOLDER, profile + ".xsd");
		_logger.trace("Loading {} schema from {}", profile, schema);
		if(Files.notExists(schema)){
			_logger.trace("Schema for {} is not in the local FS, downloading it", profile);
			Downloader downloader = new Downloader(getProfileURL(profile), ComponentRegistryService.SCHEMA_FOLDER + "/" + profile + ".xsd");
			downloader.download();
		}
				
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);		
		return schemaFactory.newSchema(schema.toFile());
	}
	
	
	public synchronized String getProfileURL(String profileId) {
		return ComponentRegistryService.CLARIN_COMPONENT_REGISTRY_REST_URL + ComponentRegistryService.PROFILE_PREFIX + profileId + "/xsd";
	}
}
