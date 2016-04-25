package eu.clarin.web.utils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CurationEntityType;

public class XSLTTransformer {
	
	static final Logger _logger = LoggerFactory.getLogger(XSLTTransformer.class);
	
	Transformer profileTransformer = null;
	Transformer instanceTransformer = null;
	Transformer collectionTransformer = null;
	
	public XSLTTransformer(){
		TransformerFactory factory = TransformerFactory.newInstance();
		try {
			profileTransformer = factory.newTemplates(new StreamSource(XSLTTransformer.class.getResourceAsStream("/xslt/XML2HTMLProfile.xsl"))).newTransformer();
		} catch (TransformerConfigurationException e) {
			_logger.error("Unable to create XSLT transformer for profiles! A possible reason could be that xslt/XML2HTMLProfile.xsl is missing or it is invalid.\n", e);
		}
		
		try {
			instanceTransformer = factory.newTemplates(new StreamSource(XSLTTransformer.class.getResourceAsStream("/xslt/XML2HTMLInstance.xsl"))).newTransformer();
		} catch (TransformerConfigurationException e) {
			_logger.error("Unable to create XSLT transformer for instances! A possible reason could be that xslt/XML2HTMLInstance.xsl is missing or it is invalid.\n", e);
		}
		
		try {
			collectionTransformer = factory.newTemplates(new StreamSource(XSLTTransformer.class.getResourceAsStream("/xslt/XML2HTMLCollection.xsl"))).newTransformer();
		} catch (TransformerConfigurationException e) {
			_logger.error("Unable to create XSLT transformer for collections! A possible reason could be that xslt/XML2HTMLCollection.xsl is missing or it is invalid.\n", e);
		}
	}
	
	public String transform(CurationEntityType type, String content){
		Source source = new StreamSource(new StringReader(content));
		StreamResult result = new StreamResult(new StringWriter());
		try{
			switch(type){				
				case PROFILE:
					if(profileTransformer != null)
						profileTransformer.transform(source, result);
					break;
				case INSTANCE:
					if(instanceTransformer != null)
						instanceTransformer.transform(source, result);
					break;
				case COLLECTION:
					if(collectionTransformer != null)
						collectionTransformer.transform(source, result);
					break;
			}
			
			return result.getWriter().toString();
		
		}catch(TransformerException e){
			_logger.error("Errow while tranforming {}", type.toString(), e);
			return content;
		}
	}	

}
