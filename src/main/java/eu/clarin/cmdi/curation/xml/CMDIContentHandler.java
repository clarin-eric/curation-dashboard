package eu.clarin.cmdi.curation.xml;

import java.util.Collection;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.entities.CMDIRecordValue;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 * 
 * A customized ContentHandler to count number of elements
 *
 */

public class CMDIContentHandler extends DefaultHandler{        
		
	Report report;
	CMDIRecord instance;
	
	int numOfElements = 0;
	int numOfSimpleElements = 0;
	int numOfEmptyElements = 0;
    
    String curElem;
    boolean elemWithValue;
    
    Locator locator;
    
    Collection<CMDIRecordValue> values = new LinkedList<CMDIRecordValue>();
    
    
    
    //for handling attributes
//	private TypeInfoProvider provider;
//  public CMDIContentHandler(TypeInfoProvider provider) {
//      this.provider = provider;
//  }
    
    public CMDIContentHandler(CMDIRecord instance, Report report){
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
    	
    	numOfElements++;
    	curElem = qName;
    	
    	//handle attributes
//        TypeInfo etype = provider.getElementTypeInfo();
//        //sb.append(" of type {" + etype.getTypeNamespace() + '}' + etype.getTypeName());
//        for (int a=0; a<attributes.getLength(); a++) {
//            TypeInfo atype = provider.getAttributeTypeInfo(a);
//            boolean spec = provider.isSpecified(a);
//            //sb.append("Attribute " + attributes.getQName(a) + (spec ? " (specified)" : (" (defaulted)")));
//            if (atype == null) {
//                //sb.append(" of unknown type");
//            } else {
//                //sb.append(" of type {" + atype.getTypeNamespace() + '}' + atype.getTypeName());
//            }
//        }
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
    	
    	if(curElem.equals(qName)){//is a simple elem
    		numOfSimpleElements++;
    		if(!elemWithValue){//does it have a value
    			numOfEmptyElements++; 
    			report.addMessage(new Message(Severity.WARNING, locator.getLineNumber(), locator.getColumnNumber(), "Simple element \"" + qName + "\" doesn't contain any value"));
    		}
    	}
    			
    	elemWithValue = false;
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    	elemWithValue = true;
    	//mark MDSelflinks and ResourceProxy Links
    	values.add(new CMDIRecordValue(new String(ch, start, length), (curElem.equals("MdSelfLink") || curElem.equals("ResourceRef"))? curElem : null));
    	
    	
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
    	report.addMessage("Total number of elements:" + numOfElements);
    	report.addMessage("Total number of simple elements:" + numOfSimpleElements);
    	report.addMessage("Total number of empty elements:" + numOfEmptyElements); 
    	report.addMessage("Filled in ratio: " + (numOfSimpleElements - numOfEmptyElements)*1.0/numOfSimpleElements*100 + "%"); 
    	instance.setValues(values);
    	
    }
    

}
