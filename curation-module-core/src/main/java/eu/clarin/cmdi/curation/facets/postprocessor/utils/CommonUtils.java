package eu.clarin.cmdi.curation.facets.postprocessor.utils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.facets.FacetConstants;

public final class CommonUtils {
	
	private final static Logger LOG = LoggerFactory.getLogger(CommonUtils.class);

    private final static Set<String> ANNOTATION_MIMETYPES = new HashSet<>();
    
    static final Map<String, Map<String, String>> cmdiComponentItemMapCache = new HashMap<>();

    static {
        ANNOTATION_MIMETYPES.add("text/x-eaf+xml");
        ANNOTATION_MIMETYPES.add("text/x-shoebox-text");
        ANNOTATION_MIMETYPES.add("text/x-toolbox-text");
        ANNOTATION_MIMETYPES.add("text/x-chat");
        ANNOTATION_MIMETYPES.add("text/x-chat");
        ANNOTATION_MIMETYPES.add("application/mediatagger");
        ANNOTATION_MIMETYPES.add("mt");
        ANNOTATION_MIMETYPES.add("application/smil+xml");
    }
    private final static Set<String> TEXT_MIMETYPES = new HashSet<String>();

    static {
        TEXT_MIMETYPES.add("application/pdf");
        TEXT_MIMETYPES.add("txt");
    }
    private final static Set<String> VIDEO_MIMETYPES = new HashSet<String>();

    static {
        VIDEO_MIMETYPES.add("application/mxf");
    }
    private final static Set<String> AUDIO_MIMETYPES = new HashSet<String>();

    static {
        AUDIO_MIMETYPES.add("application/ogg");
        AUDIO_MIMETYPES.add("wav");
    }

    /**
     * Set system property {@code vlo.swallowLookupErrors} to 'true' to make
     * run/import possible without a network connection
     *
     * @return whether {@code vlo.swallowLookupErrors} equals 'true'
     */
    public static Boolean shouldSwallowLookupErrors() {
        final String propVal = System.getProperty("vlo.swallowLookupErrors", "false");
        return Boolean.valueOf(propVal);
    }

    private CommonUtils() {
    }

    public static String normalizeMimeType(String mimeType) {
        String type = mimeType;
        if (type != null) {
            type = type.toLowerCase();
        } else {
            type = "";
        }
        String result = "unknown type";
        if (ANNOTATION_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_ANNOTATION;
        } else if (type.startsWith("audio") || AUDIO_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_AUDIO;
        } else if (type.startsWith("video") || VIDEO_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_VIDEO;
        } else if (type.startsWith("image")) {
            result = FacetConstants.RESOURCE_TYPE_IMAGE;
        } else if (type.startsWith("audio")) {
            result = FacetConstants.RESOURCE_TYPE_AUDIO;
        } else if (type.startsWith("text") || TEXT_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_TEXT;
        }
        return result;
    }

    /**
     * Create a mapping out of simple CMDI components for instance: lists of
     * items: <item AppInfo="Tigrinya (ti)">ti</item> Will become key (after
     * removal of trailing 2 or 3 letter codes), values: ti, Tigrinya
     *
     * @param urlToComponent
     * @return Map with item_value, AppInfo_value pairs
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public synchronized static Map<String, String> createCMDIComponentItemMap(String urlToComponent) throws Exception {
    	
    	//check in cache
    	Map<String, String> result;
    	result = cmdiComponentItemMapCache.get(urlToComponent);
    	if(result != null)
    		return result;
    	
    	LOG.info("Creating code map from {}", urlToComponent);
    	//not in cache
        result = new ConcurrentHashMap<String, String>();
        
        
        VTDGen parser = new VTDGen();
		if (!parser.parseHttpUrl(urlToComponent, true))
			throw new Exception("Errors while parsing " + urlToComponent);
		VTDNav navigator = parser.getNav();
		AutoPilot ap = new AutoPilot(navigator);
		ap.selectXPath("//item");
		while(true){
            int index = ap.evalXPath();
			if (index != -1){
				String shortName = navigator.getXPathStringVal();
				String longName = navigator.toString(navigator.getAttrVal("AppInfo")).replaceAll(" \\([a-zA-Z]+\\)$", "");								
				result.put(shortName.toUpperCase(), longName);
			}
			else
				break;
		}
        
        //add to cache
        cmdiComponentItemMapCache.put(urlToComponent, result);
        return result;
    }

    /**
     * Create a mapping out of simple CMDI components for instance: lists of
     * items: <item AppInfo="Tigrinya">ti</item> Will become key (after removal
     * of trailing 2 or 3 letter codes), values: Tigrinya, ti
     *
     * @param urlToComponent
     * @return Map with item_value, AppInfo_value pairs
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static Map<String, String> createReverseCMDIComponentItemMap(String urlToComponent) throws XPathExpressionException, SAXException,
            IOException, ParserConfigurationException {
        Map<String, String> result = new HashMap<String, String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        URL url = new URL(urlToComponent);
        //TODO: Process XML as stream for much better performance (no need to build entire DOM)
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(url.openStream());
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate("//item", doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String shortName = node.getTextContent();
            String longName = node.getAttributes().getNamedItem("AppInfo").getNodeValue().replaceAll(" \\([a-zA-Z]+\\)$", "");
            result.put(longName, shortName);
        }
        return result;
    }

}
