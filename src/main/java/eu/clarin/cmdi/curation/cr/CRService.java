package eu.clarin.cmdi.curation.cr;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.clarin.cmdi.curation.io.Downloader;
import eu.clarin.cmdi.curation.utils.Triplet;

public class CRService implements IComponentRegistryService { // ,XMLMarshaller<CRProfiles>

    static final Logger _logger = LoggerFactory.getLogger(CRService.class);
    
    //singleton
    private static CRService instance = new CRService();


    private final Map<String, Triplet<File, Schema, Exception>> schemaCache = new ConcurrentHashMap<>();
    private final Map<String, Object> beingProcessed = new ConcurrentHashMap<>();
    
    private Collection<String> publicProfiles = null;

    final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);    

    private CRService() {
	// create xsd dir if it doesnt exist
	File xsdDir = new File(CRConstants.SCHEMA_FOLDER);
	xsdDir.mkdirs();
    }

    public static CRService getInstance() {
	return instance;
    }
    
    
    @Override
    public ProfileSpec getProfile(final String profile) throws Exception{
	JAXBContext jc = JAXBContext.newInstance(ProfileSpec.class);
	Unmarshaller unmarshaller = jc.createUnmarshaller();
	ProfileSpec profileSpec = (ProfileSpec) unmarshaller.unmarshal(new URL(CRConstants.REST_API + CRConstants.PROFILE_PREFIX  + profile).openStream());
	return profileSpec;
    }
    
    @Override
    public boolean isPublic(final String profile) throws Exception{
	if(publicProfiles == null){
	    publicProfiles = getPublicProfiles();
	}
	
	return publicProfiles.contains(profile);
    }
    
    @Override
    public Schema getSchema(final String profile) throws Exception{
	Triplet<File, Schema, Exception> triplet = getSchemaTriplet(profile);
	if (triplet.getZ() != null)
	    throw triplet.getZ();

	return triplet.getY();
    }

    @Override
    public File getLocalFile(String profile) throws Exception{
	Triplet<File, Schema, Exception> triplet = getSchemaTriplet(profile);
	if (triplet.getZ() != null)
	    throw triplet.getZ();

	return triplet.getX();
    }
    
    
    //returns ids without prefix clarin.eu:cr1:
    private Collection<String> getPublicProfiles() throws Exception {
	JAXBContext jc = JAXBContext.newInstance(CRProfiles.class);
	Unmarshaller unmarshaller = jc.createUnmarshaller();
	CRProfiles crProfiles = (CRProfiles) unmarshaller.unmarshal(new URL(CRConstants.REST_API).openStream());
	return crProfiles.profileDescription.stream()
		.map(desc -> desc.id.substring(CRConstants.PROFILE_PREFIX.length()))
		.collect(Collectors.toList());
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
			    File schemaFile = new File(CRConstants.SCHEMA_FOLDER + profile + ".xsd");
			    _logger.trace("Loading {} schema from {}", profile, schemaFile.getName());
			    if (!schemaFile.exists()) {
				// if not download it
				_logger.trace("Schema for {} is not in the local FS, downloading it", profile);
				new Downloader().download(CRConstants.REST_API + CRConstants.PROFILE_PREFIX + profile + "/xsd", schemaFile);
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


//    // test concurency
    public static void main(String[] args) throws Exception {
//	CRService crs = CRService.getInstance();

//	Collection<String> profiles = new LinkedList();
//	profiles.add("p_1357720977520");
//	profiles.add("p_1297242111880");
//	profiles.add("p_1361876010587");
//	profiles.add("p_1361876010587");
//	profiles.add("p_1357720977520");
//	profiles.add("p_1297242111880");
//	profiles.add("p_1357720977520");
//	profiles.add("p_1361876010587");
//	profiles.add("p_1297242111880");
//
//	profiles.parallelStream().forEach(profile -> {
//	    try {
//		crs.getSchema(profile);
//	    } catch (Exception e) {
//		_logger.error("", e);
//	    }
//	});
    }


}
