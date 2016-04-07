package eu.clarin.cmdi.curation.cr;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.cr.ProfileDescriptions.ProfileHeader;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.io.Downloader;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDProfileReport;
import eu.clarin.cmdi.curation.xml.CMDXPathService;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;

public class CRService implements ICRService {
	static final Logger _logger = LoggerFactory.getLogger(CRService.class);

	public static final String REST_API = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/";
	private static final String PROFILE_PREFIX = "clarin.eu:cr1:";

	private final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	private List<ProfileHeader> profileHeaders = null;

	private final Map<String, ProfileStruct> schemaCache = new ConcurrentHashMap<>();
	private final Map<String, Object> schemaBeingProcessed = new ConcurrentHashMap<>();

	private final Map<String, ScoreStruct> scoreCache = new ConcurrentHashMap<>();
	private final Map<String, Object> scoreBeingProcessed = new ConcurrentHashMap<>();

	// singleton
	private static CRService instance = new CRService();

	private CRService() {
		try{
			//cache all public profiles
			getPublicProfiles();
		}catch (Exception e) {
			throw new RuntimeException("CLARIN Component Registry REST service doesn't work as expected. Unable to continue", e);
		}
	}

	public static CRService getInstance() {
		return instance;
	}

	@Override
	public ProfileHeader getProfileHeader(final String profileId) throws Exception{
		ProfileHeader profile = profileHeaders.stream().filter(p -> p.id.equals(profileId)).findFirst().orElse(null);
		if(profile != null)
			return profile;
		
		//not public
		CMDXPathService xpathService = new CMDXPathService(REST_API + profileId);
		profile = new ProfileHeader();
		profile.id = profileId;
		profile.name = xpathService.getXPathValue("/CMD_ComponentSpec/Header/Name");
		profile.description = xpathService.getXPathValue("/CMD_ComponentSpec/Header/Description");
		profile.isPublic = false;
		
		profileHeaders.add(profile);
		
		return profile;
		
		
	}
	
	
	@Override
	public boolean isPublic(final String profileId) throws Exception {
		ProfileHeader profile = getProfileHeader(profileId);
		return profile.isPublic;			
	}
	
	@Override
	public boolean isNameUnique(String name) throws Exception {		
		return profileHeaders.stream().filter(profile -> profile.name.equals(name)).count() <= 1;
	}

	@Override
	public boolean isDescriptionUnique(String description) throws Exception {	
		return profileHeaders.stream().filter(profile -> profile.description.equals(description)).count() <= 1;
	}

	@Override
	public boolean isSchemaCRResident(final String schemaUrl) {
		return schemaUrl.startsWith(REST_API);
	}

	@Override
	public Schema getSchema(final String profileId) throws Exception {
		ProfileStruct elem = schemaLookup(profileId);
		if (elem.ex != null)
			throw elem.ex;
		return elem.schema;
	}

	@Override
	public VTDNav getParsedXSD(final String profileId) throws Exception {
		ProfileStruct elem = schemaLookup(profileId);
		if (elem.ex != null)
			throw elem.ex;
		return elem.xsd.cloneNav();
	}

	@Override
	public VTDNav getParseXML(final String profileId) throws Exception {
		ProfileStruct elem = schemaLookup(profileId);
		if (elem.ex != null)
			throw elem.ex;
		return elem.xml.cloneNav();
	}

	@Override
	public double getScore(String profileId) throws Exception {
		if (!scoreCache.containsKey(profileId)) {
			if (scoreBeingProcessed.putIfAbsent(profileId, new Object()) == null) {
				new Runnable() {
					public void run() {
						CMDProfileReport report = (CMDProfileReport) new CMDProfile(profileId).generateReport();
						if (!report.isValid)
							scoreCache.put(profileId, new ScoreStruct(new Exception("Unable to process profile"
									+ profileId + ". Run curation for this profile to see details!")));
						else
							scoreCache.put(profileId, new ScoreStruct(report.score));
						Object lock = scoreBeingProcessed.get(profileId);
						synchronized (lock) {
							lock.notifyAll();
						}
					}
				}.run();
			}

			while (!scoreCache.containsKey(profileId)) {
				Object lock = scoreBeingProcessed.get(profileId);
				synchronized (lock) {
					lock.wait();
				}
			}
		}

		ScoreStruct entry = scoreCache.get(profileId);
		if (entry.ex != null)
			throw entry.ex;

		return entry.score;
	}

	@Override
	public List<ProfileHeader> getPublicProfiles() throws Exception {		
		if(profileHeaders == null){		
			XMLMarshaller<ProfileDescriptions> marshaller = new XMLMarshaller<>(ProfileDescriptions.class);
			
			List<ProfileHeader> publicProfiles = marshaller.unmarshal(new URL(REST_API).openStream()).profileDescription;
			publicProfiles.forEach(p -> p.isPublic = true);
			profileHeaders = publicProfiles;
			return profileHeaders;
		}else{
			return profileHeaders.stream().filter(profile -> profile.isPublic).collect(Collectors.toList());			
		}
	}

	private ProfileStruct schemaLookup(final String profileId) throws InterruptedException {
		if (!schemaCache.containsKey(profileId)) {
			// if null means that key didn't existed
			if (schemaBeingProcessed.putIfAbsent(profileId, new Object()) == null) {
				new Runnable() {

					@Override
					public void run() {
						ProfileStruct elem = null;
						Path xsd = null;
						Path xml = null;
						try {
							// resolve xsd
							String fileName = profileId.substring(PROFILE_PREFIX.length());
							xsd = Configuration.CACHE_DIRECTORY.resolve(fileName + ".xsd");
							_logger.trace("Loading {} schema from {}", profileId, xsd);
							if (!Files.exists(xsd)) {
								// if not download it
								Files.createFile(xsd);
								_logger.info("{}/xsd is not in the local cache, it will be downloaded", profileId);
								new Downloader().download(REST_API + profileId + "/xsd", xsd.toFile());
							}

							// resolve xml
							xml = Configuration.CACHE_DIRECTORY.resolve(fileName + ".xml");
							_logger.trace("Loading {} xml from {}", profileId, xml);
							if (!Files.exists(xml)) {
								// if not download it
								Files.createFile(xml);
								_logger.info("{}/xml is not in the local cache, it will be downloaded", profileId);
								new Downloader().download(REST_API + profileId + "/xml", xml.toFile());
							} else {// not public, dont cache it

							}

							VTDGen parser = new VTDGen();
							parser.setDoc(Files.readAllBytes(xsd));
							parser.parse(true);
							VTDNav parsedXSD = parser.getNav();

							parser.setDoc(Files.readAllBytes(xml));
							parser.parse(true);
							VTDNav parsedXML = parser.getNav();

							Schema schema = createSchema(xsd.toFile());

							elem = new ProfileStruct(isPublic(profileId), parsedXSD, parsedXML, schema);

						} catch (Exception e) {
							_logger.error("Error while caching schema for {}. XSD and XML files will be removed!",
									profileId, e);
							elem = new ProfileStruct(false, e);
							try {
								Files.delete(xsd);
								Files.delete(xml);
							} catch (Exception e1) {
								// do nothing
							}

						} finally {
							schemaCache.putIfAbsent(profileId, elem);
							Object lock = schemaBeingProcessed.get(profileId);
							synchronized (lock) {
								lock.notifyAll();
							}

							// dont keep files if profile is not public
							try {
								if (!isPublic(profileId)) {
									_logger.warn("Profile {} is not public. XSD and XML files won't be kept on disk",
											profileId);
									Files.delete(xsd);
									Files.delete(xml);
								}
							} catch (Exception e) {
								// do nothing
							}

						}
					}
				}.run();
			} // end if is currently processed

			while (!schemaCache.containsKey(profileId)) {
				Object lock = schemaBeingProcessed.get(profileId);
				synchronized (lock) {
					lock.wait();
				}
			}

		} // end if not in cache

		return schemaCache.get(profileId);

	}

	synchronized private Schema createSchema(File schemaFile) throws SAXException {
		return schemaFactory.newSchema(schemaFile);
	}

	synchronized private Schema createSchema(URL schemaURL) throws SAXException {
		return schemaFactory.newSchema(schemaURL);
	}

	class ProfileStruct {
		VTDNav xsd = null;

		VTDNav xml = null;

		Schema schema = null;

		Exception ex = null;

		boolean isPublic = false;

		ProfileStruct(boolean isPublic, VTDNav xsd, VTDNav xml, Schema schema) {
			this.isPublic = isPublic;
			this.xsd = xsd;
			this.xml = xml;
			this.schema = schema;
		}

		ProfileStruct(boolean isPublic, Exception ex) {
			this.isPublic = isPublic;
			this.ex = ex;
		}
	}

	class ScoreStruct {
		double score = 0;

		Exception ex = null;

		ScoreStruct(double score) {
			this.score = score;
		}

		ScoreStruct(Exception exception) {
			this.ex = exception;
		}
	}

}
