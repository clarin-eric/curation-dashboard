package eu.clarin.cmdi.curation.xml;

import java.util.Collection;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.curation.entities.CMDIRecord;

/**
 * @author dostojic
 * 
 * A customized ContentHandler to count number of elements
 *
 */

public class CMDIContentHandler extends DefaultHandler{        
		
	CMDIRecord instance;
	
	int numOfElements = 0;
	int numOfSimpleElements = 0;
	int numOfEmptyElements = 0;
    
    String curElem;
    boolean elemWithValue;
    
    Collection<String> values = new LinkedList<String>();
    
    
    
    //for handling attributes
//	private TypeInfoProvider provider;
//  public CMDIContentHandler(TypeInfoProvider provider) {
//      this.provider = provider;
//  }
    
    public CMDIContentHandler(CMDIRecord instance){
    	this.instance = instance;
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
    		if(!elemWithValue)//does it have value
    			numOfEmptyElements++;
    	}
    			
    	elemWithValue = false;
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    	elemWithValue = true;
    	values.add(new String(ch, start, length));
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
    	instance.setNumOfElements(numOfElements);
    	instance.setNumOfSimpleElements(numOfSimpleElements);
    	instance.setNumOfEmptyElements(numOfEmptyElements);
    	
    	instance.setValues(values);
    	
    }

}
