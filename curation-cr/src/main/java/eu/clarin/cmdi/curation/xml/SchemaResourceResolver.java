package eu.clarin.cmdi.curation.xml;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class SchemaResourceResolver implements LSResourceResolver {
	
	SchemaInput si = new SchemaInput();

	@Override
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		if(systemId != null && systemId.equals("http://www.w3.org/2001/xml.xsd")){
			return si;
		}
		
		return null;
	}

}
