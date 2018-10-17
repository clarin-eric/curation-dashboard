package eu.clarin.cmdi.curation.cr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;

import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParser;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileParserFactory;
import eu.clarin.cmdi.curation.io.Downloader;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.xml.SchemaResourceResolver;

class ProfileCacheFactory{
	
	static final long HOUR_IN_NS = 3600000000000L;
	
	private static final Logger logger = LoggerFactory.getLogger(ProfileCacheFactory.class);
	
	private static final SchemaFactory schemaFactory;
	
	static{
		schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFactory.setResourceResolver(new SchemaResourceResolver());
	}
	
	public static LoadingCache<ProfileHeader, ProfileCacheEntry> createProfileCache(boolean isPublicProfilesCache){
		return isPublicProfilesCache?
		
		CacheBuilder.newBuilder()
				.concurrencyLevel(4)
				.build(new ProfileCacheLoader(isPublicProfilesCache))
		:
		CacheBuilder.newBuilder()
			.concurrencyLevel(4)
			.expireAfterWrite(8, TimeUnit.HOURS)//keep non public profiles 8 hours in cache
			.ticker(new Ticker() { @Override public long read() { return 9 * HOUR_IN_NS; } }) //cache tick 9 hours
			.build(new ProfileCacheLoader(isPublicProfilesCache));		
	}
	
	private static synchronized Schema createSchema(File schemaFile) throws SAXException {
		return schemaFactory.newSchema(schemaFile);
	}
	
	
	
	
	private static class ProfileCacheLoader extends CacheLoader<ProfileHeader, ProfileCacheEntry>{

		final boolean isPublicProfilesCache;
		
		public ProfileCacheLoader(boolean isPublicProfilesCache){
			this.isPublicProfilesCache = isPublicProfilesCache;
		}
		
		
		@Override
		public ProfileCacheEntry load(ProfileHeader header) throws IOException, VTDException, SAXException{			
			logger.info("Profile {} is not in the cache, it will be loaded", header.id);
			
			Path xsd;			
			
			if(isPublicProfilesCache){			
				
				String fileName = header.id.substring(CRService.PROFILE_PREFIX.length());
				xsd = Configuration.CACHE_DIRECTORY.resolve(fileName + ".xsd");
				//try to load it from the disk
				logger.debug("profile {} is public. Loading schema from {}", header.id, xsd);
				if (!Files.exists(xsd)) {// keep public profiles on disk 
					// if not download it
					Files.createFile(xsd);
					logger.info("XSD for the {} is not in the local cache, it will be downloaded", header.id);
					new Downloader().download(Configuration.vloConfig.getComponentRegistryProfileSchema(header.id), xsd.toFile());
				}
			}
			else{//non-public profiles are not cached on disk
				logger.debug("schema {} is not public. Schema will be downloaded in temp folder", header.id);
				
				//keep private schemas on disk
								
				
				String fileName = header.id.substring(CRService.PROFILE_PREFIX.length());
				xsd = Configuration.CACHE_DIRECTORY.resolve("private_profiles");
				xsd = xsd.resolve(fileName + ".xsd");
				//try to load it from the disk
				logger.debug("Loading schema for non public profile {} from {}", header.id, xsd);
				if (!Files.exists(xsd)) {
					// if not download it
					Files.createFile(xsd);
					logger.info("XSD for the {} is not in the local cache, it will be downloaded", header.id);
					new Downloader().download(Configuration.vloConfig.getComponentRegistryProfileSchema(header.id), xsd.toFile());
					
				}
			}
			
			VTDGen vg = new VTDGen();
			vg.setDoc(Files.readAllBytes(xsd));
			vg.parse(true);
			
			ProfileParser parser = ProfileParserFactory.createParser(header.cmdiVersion);
			
			ParsedProfile parsedProfile = parser.parse(vg.getNav(), header);
			Schema schema = createSchema(xsd.toFile());
			
			//facetMapping
			
			return new ProfileCacheEntry(parsedProfile, schema);
		}
		
	}
	
	static class ProfileCacheEntry{
		ParsedProfile parsedProfile;
		Schema schema;
		
		public ProfileCacheEntry(ParsedProfile parsedProfile, Schema schema) {
			this.parsedProfile = parsedProfile;
			this.schema = schema;
		}		
	}

}
