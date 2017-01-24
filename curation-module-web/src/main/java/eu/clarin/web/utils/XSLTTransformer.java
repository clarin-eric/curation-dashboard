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

import eu.clarin.cmdi.curation.entities.CurationEntity.CurationEntityType;

public class XSLTTransformer {
	
	static final Logger _logger = LoggerFactory.getLogger(XSLTTransformer.class);
	
	private static Transformer profileTransformer = null;
	private static Transformer instanceTransformer = null;
	private static Transformer collectionTransformer = null;
	
	static{
		TransformerFactory factory = TransformerFactory.newInstance();
		try{
			profileTransformer = factory.newTemplates(new StreamSource(XSLTTransformer.class.getResourceAsStream("/xslt/XML2HTMLProfile.xsl"))).newTransformer();
			instanceTransformer = factory.newTemplates(new StreamSource(XSLTTransformer.class.getResourceAsStream("/xslt/XML2HTMLInstance.xsl"))).newTransformer();
			collectionTransformer = factory.newTemplates(new StreamSource(XSLTTransformer.class.getResourceAsStream("/xslt/XML2HTMLCollection.xsl"))).newTransformer();
		} catch (TransformerConfigurationException e) {
			_logger.error("Unable to create XSLT transformers!", e);
		}
	
	}
	
	public synchronized String transform(CurationEntityType type, String content){
		Transformer t = null;
		switch(type){				
			case PROFILE:
				if(profileTransformer != null)
					t = profileTransformer;
				break;
			case INSTANCE:
				if(instanceTransformer != null)
					t = instanceTransformer;
				break;
			case COLLECTION:
				if(collectionTransformer != null)
					t = collectionTransformer;
				break;
		}
					
			if(t != null){
				Source source = new StreamSource(new StringReader(content));
				StreamResult result = new StreamResult(new StringWriter());
				try{
					t.transform(source, result);
					return result.getWriter().toString();
				}catch(TransformerException e){
					_logger.error("Errow while tranforming {}", type.toString(), e);					
				}				
			}
			return content = "XSL file is missing or invalid, please download report in xml form\n\n";
	}

}
