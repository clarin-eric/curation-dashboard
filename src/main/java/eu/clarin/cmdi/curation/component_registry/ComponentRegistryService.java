package eu.clarin.cmdi.curation.component_registry;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.clarin.cmdi.curation.entities.CMDIProfile;
import eu.clarin.cmdi.curation.io.Downloader;
import eu.clarin.cmdi.curation.utils.Triplet;

public class ComponentRegistryService implements IComponentRegistryService { // ,XMLMarshaller<CCRProfiles>

    static final Logger _logger = LoggerFactory.getLogger(ComponentRegistryService.class);
    
    //singleton
    private static ComponentRegistryService instance = new ComponentRegistryService();

    //move this to config
    public static final String CCR_REST = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/";
    public static final String PROFILE_PREFIX = "clarin.eu:cr1:";
    public static final String SCHEMA_FOLDER = "D:/xsd/";

    private Collection<CMDIProfile> profiles = new LinkedList<>();

    private final Map<String, Triplet<File, Schema, Exception>> schemaCache = new ConcurrentHashMap<>();// XSDCache.getInstance();
    private final Map<String, Object> beingProcessed = new ConcurrentHashMap<>();

    final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    

    private ComponentRegistryService() {
	// create xsd dir if it doesnt exist
	File xsdDir = new File(SCHEMA_FOLDER);
	xsdDir.mkdirs();

	// if(clearCache)
	// clearCache
    }

    public static ComponentRegistryService getInstance() {
	return instance;
    }

    public CMDIProfile getProfile(final String profile) throws Exception {
	return null;
    }

    @Override
    public Collection<CMDIProfile> getPublicProfiles() throws Exception {
	JAXBContext jc = JAXBContext.newInstance(CCRProfiles.class);
	Unmarshaller unmarshaller = jc.createUnmarshaller();
	CCRProfiles ccrProfiles = (CCRProfiles) unmarshaller.unmarshal(new URL(CCR_REST).openStream());
	return ccrProfiles.profileDescription.stream().map(ProfileDescription::toCMDIProfile)
		.collect(Collectors.toList());

    }

    public Schema getSchema(final String profile) throws Exception {
	Triplet<File, Schema, Exception> triplet = getSchemaTriplet(profile);
	if (triplet.getZ() != null)
	    throw triplet.getZ();

	return triplet.getY();
    }

    public File getSchemaFile(String profile) throws Exception {
	Triplet<File, Schema, Exception> triplet = getSchemaTriplet(profile);
	if (triplet.getZ() != null)
	    throw triplet.getZ();

	return triplet.getX();
    }

    private Triplet<File, Schema, Exception> getSchemaTriplet(final String profile) throws InterruptedException {
	if (!schemaCache.containsKey(profile)) {
	    // if null means that key didn't existed
	    if (beingProcessed.putIfAbsent(profile, new Object()) == null) {
		new Thread() {
		    @Override
		    public void run() {
			Triplet<File, Schema, Exception> triplet = new Triplet<>();
			try {
			    // check if file is on disk
			    File schemaFile = new File(SCHEMA_FOLDER + profile + ".xsd");
			    _logger.trace("Loading {} schema from {}", profile, schemaFile.getName());
			    if (!schemaFile.exists()) {
				// if not download it
				_logger.trace("Schema for {} is not in the local FS, downloading it", profile);
				new Downloader().download(CCR_REST + PROFILE_PREFIX + profile + "/xsd", schemaFile);
			    }

			    triplet.setX(schemaFile);
			    triplet.setY(createSchema(schemaFile));

			} catch (Exception e) {
			    triplet.setZ(e);
			}

			schemaCache.putIfAbsent(profile, triplet);
			Object lock = beingProcessed.get(profile);
			synchronized (lock) {
			    lock.notifyAll();
			}
		    }
		}.start();
	    }
	}

	while (!schemaCache.containsKey(profile)) {
	    Object lock = beingProcessed.get(profile);
	    synchronized (lock) {
		lock.wait();
	    }
	}

	return schemaCache.get(profile);

    }

    synchronized private Schema createSchema(File schemaFile) throws SAXException {
	return schemaFactory.newSchema(schemaFile);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "profileDescriptions")
    private static class CCRProfiles {
	@XmlElement
	Collection<ProfileDescription> profileDescription;

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static class ProfileDescription {

	@XmlElement
	String id;

	@XmlElement
	String name;

	public CMDIProfile toCMDIProfile() {
	    CMDIProfile cmdi = new CMDIProfile(null);
	    cmdi.setId(this.id);
	    cmdi.setName(this.name);
	    cmdi.setUrl(CCR_REST + "/" + id);
	    cmdi.setPublic(true);

	    return cmdi;
	}
    }

    public static void test2(String[] args) {

	ComponentRegistryService crs = ComponentRegistryService.getInstance();
	try {
	    crs.getPublicProfiles().forEach(System.out::println);
	} catch (Exception e) {
	    System.out.println("unable to download xsd schemas");
	    e.printStackTrace();
	}
    }

    // test concurency
    public static void test1() {
	ComponentRegistryService crs = ComponentRegistryService.getInstance();

	Collection<String> profiles = new LinkedList();
	profiles.add("p_1357720977520");
	profiles.add("p_1297242111880");
	profiles.add("p_1361876010587");
	profiles.add("p_1361876010587");
	profiles.add("p_1357720977520");
	profiles.add("p_1297242111880");
	profiles.add("p_1357720977520");
	profiles.add("p_1361876010587");
	profiles.add("p_1297242111880");

	profiles.parallelStream().forEach(profile -> {
	    try {
		crs.getSchema(profile);
	    } catch (Exception e) {
		_logger.error("", e);
	    }
	});
    }

    public static void main(String[] args) {
	test1();
    }

}
