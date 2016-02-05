package eu.clarin.cmdi.curation.xml;

import java.util.Collection;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.entities.CMDIUrlNode;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 * 
 *         A customized ContentHandler to count number of elements
 *
 */

public class CMDIContentHandler extends DefaultHandler {

    CMDIRecord instance;

    int numOfElements = 0;
    int numOfSimpleElements = 0;
    int numOfEmptyElements = 0;
    
    int numOfLinks = 0;
    int numOfResProxyLinks = 0;

    String curElem;
    boolean elemWithValue;

    Locator locator;

    Collection<CMDIUrlNode> values = new LinkedList<CMDIUrlNode>();

    // for handling attributes
    // private TypeInfoProvider provider;
    // public CMDIContentHandler(TypeInfoProvider provider) {
    // this.provider = provider;
    // }

    public CMDIContentHandler(CMDIRecord instance) {
	this.instance = instance;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
	this.locator = locator;
    }

    /**
     * Receive notification of the start of an element.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

	numOfElements++;
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
	    numOfSimpleElements++;
	    if (!elemWithValue) {// does it have a value
		numOfEmptyElements++;
		String msg = "Empty element <" + qName + "> was found on line " + locator.getLineNumber();
		instance.addDetail(Severity.WARNING, msg);
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
	    numOfLinks++;
	    CMDIUrlNode node = new CMDIUrlNode(val, (curElem.equals("MdSelfLink") || curElem.equals("ResourceRef")) ? curElem : null);
	    if(!values.contains(node))
		values.add(node);
	    
	    if(curElem.equals("MdSelfLink"))
		numOfResProxyLinks++;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
	instance.setNumOfXMLElements(numOfElements);
	instance.setNumOfXMLSimpleElements(numOfSimpleElements);
	instance.setNumOfXMLEmptyElement(numOfEmptyElements);
	instance.setNumOfLinks(numOfLinks);
	instance.setNumOfResProxiesLinks(numOfResProxyLinks);
	instance.setNumOfUniqueLinks(values.size());
	instance.setLinks(values);

    }

}
