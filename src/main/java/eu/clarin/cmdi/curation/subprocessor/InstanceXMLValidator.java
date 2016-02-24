package eu.clarin.cmdi.curation.subprocessor;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.ValidatorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.curation.component_registry.ComponentRegistryService;
import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.entities.CMDIUrlNode;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDIErrorHandler;

public class InstanceXMLValidator extends CMDISubprocessor {

    static final Logger _logger = LoggerFactory.getLogger(InstanceXMLValidator.class);

    @Override
    public boolean process(CMDIInstance entity, CMDIInstanceReport report) {
	try {
	    ValidatorHandler schemaValidator = ComponentRegistryService.getInstance().getSchema(report.profile)
		    .newValidatorHandler();
	    schemaValidator.setErrorHandler(new CMDIErrorHandler(report));
	    schemaValidator.setContentHandler(new CMDIInstanceContentHandler(entity, report));
	    // setValidationFeatures(schemaValidator);
	    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	    parserFactory.setNamespaceAware(true);
	    SAXParser parser = parserFactory.newSAXParser();
	    XMLReader reader = parser.getXMLReader();
	    reader.setContentHandler(schemaValidator);
	    reader.parse(new InputSource(entity.getPath().toUri().toString()));
	    return true;
	} catch (Exception e) {
	    _logger.error("Error processing XSD schema with ID:  " + report.profile, e);
	    report.addDetail(Severity.FATAL,
		    "Error processing XSD schema with ID:  " + report.profile + ", " + e.getMessage());
	    return false;
	}
    }

    /*
     * set custom xerces features here, if u really need them
     * 
     */

    private void setValidationFeatures(ValidatorHandler validator) {
	try {
	    validator.setFeature("http://apache.org/xml/features/warn-on-duplicate-entitydef", true);
	    validator.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
	    validator.setFeature("http://xml.org/sax/features/validation", true);
	    validator.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
	} catch (Exception e) {
	    _logger.warn("feature is not supported", e);
	}
    }
    
    
    class CMDIInstanceContentHandler extends DefaultHandler {

	    CMDIInstance instance;
	    CMDIInstanceReport report;

	    String curElem;
	    boolean elemWithValue;

	    Locator locator;

	    Collection<CMDIUrlNode> values = new LinkedList<CMDIUrlNode>();

	    // for handling attributes
	    // private TypeInfoProvider provider;
	    // public CMDIContentHandler(TypeInfoProvider provider) {
	    // this.provider = provider;
	    // }

	    public CMDIInstanceContentHandler(CMDIInstance instance, CMDIInstanceReport report) {
		this.instance = instance;
		this.report = report;
	    }

	    @Override
	    public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	    }

	    /**
	     * Receive notification of the start of an element.
	     */
	    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		report.numOfXMLElements++;
		curElem = qName;

		// handle attributes
		// TypeInfo etype = provider.getElementTypeInfo();
		// //sb.append(" of type {" + etype.getTypeNamespace() + '}' +
		// etype.getTypeName());
		// for (int a=0; a<attributes.getLength(); a++) {
		// TypeInfo atype = provider.getAttributeTypeInfo(a);
		// boolean spec = provider.isSpecified(a);
		// //sb.append("Attribute " + attributes.getQName(a) + (spec ? "
		// (specified)" : (" (defaulted)")));
		// if (atype == null) {
		// //sb.append(" of unknown type");
		// } else {
		// //sb.append(" of type {" + atype.getTypeNamespace() + '}' +
		// atype.getTypeName());
		// }
		// }
	    }

	    /**
	     * Receive notification of the end of an element.
	     */
	    public void endElement(String uri, String localName, String qName) throws SAXException {

		if (curElem.equals(qName)) {// is a simple elem
		    report.numOfXMLSimpleElements++;
		    if (!elemWithValue) {// does it have a value
			report.numOfXMLEmptyElement++;
			String msg = "Empty element <" + qName + "> was found on line " + locator.getLineNumber();
			report.addDetail(Severity.WARNING, msg);
		    }
		}

		elemWithValue = false;
	    }

	    @Override
	    public void characters(char[] ch, int start, int length) throws SAXException {
		elemWithValue = true;
		// keeping all values consumes a lot of mem
		// keep only links for URL validation
		// mark MDSelflinks and ResourceProxy Links
		String val = new String(ch, start, length);
		if (val.startsWith("http://") || val.startsWith("https://")){
		    report.numOfLinks++;
		    CMDIUrlNode node = new CMDIUrlNode(val, (curElem.equals("MdSelfLink") || curElem.equals("ResourceRef")) ? curElem : null);
		    if(!values.contains(node))
			values.add(node);
		    
		    if(curElem.equals("MdSelfLink"))
			report.numOfResProxiesLinks++;
		}
	    }

	    /*
	     * (non-Javadoc)
	     * 
	     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	     */
	    @Override
	    public void endDocument() throws SAXException {
		report.numOfUniqueLinks = values.size();
		instance.setLinks(values);

	    }
    }

}
