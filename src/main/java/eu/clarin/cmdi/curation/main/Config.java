package eu.clarin.cmdi.curation.main;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement (name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

    private static transient Logger _logger = LoggerFactory.getLogger(Config.class);    

    private transient  final String SCORE_NUMERIC_DISPLAY_FORMAT = "#0.000";

    // VLO restriction
    private long MAX_SIZE_OF_FILE;

    // huge performance impact, use it only for smaller collections <100
    private boolean HTTP_VALIDATION;

    private boolean GENERATE_CHILDREN_REPORTS;

    private String OUTPUT_DIRECTORY;
    
    static private Config configuration = null;    
    

    static public long MAX_SIZE_OF_FILE() {
        return configuration.MAX_SIZE_OF_FILE;
    }

    static public void setMAX_SIZE_OF_FILE(long mAX_SIZE_OF_FILE) {
	configuration.MAX_SIZE_OF_FILE = mAX_SIZE_OF_FILE;
    }

    static public boolean HTTP_VALIDATION() {
        return configuration.HTTP_VALIDATION;
    }

    static public void setHTTP_VALIDATION(boolean hTTP_VALIDATION) {
	configuration.HTTP_VALIDATION = hTTP_VALIDATION;
    }

    static public boolean GENERATE_CHILDREN_REPORTS() {
        return configuration.GENERATE_CHILDREN_REPORTS;
    }

    static public void setGENERATE_CHILDREN_REPORTS(boolean gENERATE_CHILDREN_REPORTS) {
	configuration.GENERATE_CHILDREN_REPORTS = gENERATE_CHILDREN_REPORTS;
    }

    static public String OUTPUT_DIRECTORY() {
        return configuration.OUTPUT_DIRECTORY;
    }

    static public void setOUTPUT_DIRECTORY(String oUTPUT_DIRECTORY) {
	configuration.OUTPUT_DIRECTORY = oUTPUT_DIRECTORY;
    }

    static public String SCORE_NUMERIC_DISPLAY_FORMAT() {
        return configuration.SCORE_NUMERIC_DISPLAY_FORMAT;
    }

    public static void init(String path) {
	try {
	    InputStream is = new FileInputStream(path);
	    Config.unmarshal(is);
	} catch (Exception e) {
	    _logger.error("Error reading configuration file {}!", path, e);
	    //fallbacks
	    initDefault();	    
	}
    }

    public static void initDefault() {
	 _logger.warn("Reading configuration file from config.xml with default values");	
	try {
	    InputStream is =  new FileInputStream("config.xml");
	    Config.unmarshal(is);
	} catch (Exception e1) {
	    throw new RuntimeException("Unable to read default configuration file", e1);
	}
    }

    public static void unmarshal(InputStream is) throws JAXBException {
	JAXBContext jc = JAXBContext.newInstance(Config.class);
	Unmarshaller unmarshaller = jc.createUnmarshaller();
	configuration =  (Config) unmarshaller.unmarshal(is);
    }
    
}
