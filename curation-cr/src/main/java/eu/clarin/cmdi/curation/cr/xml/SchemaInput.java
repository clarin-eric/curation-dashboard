package eu.clarin.cmdi.curation.cr.xml;

import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.Reader;

public class SchemaInput implements LSInput{
	

	@Override
	public Reader getCharacterStream() {

		return null;
	}

	@Override
	public void setCharacterStream(Reader characterStream) {

		
	}

	@Override
	public InputStream getByteStream() {
		
		return SchemaInput.class.getResourceAsStream("/xml.xsd");
	}

	@Override
	public void setByteStream(InputStream byteStream) {

		
	}

	@Override
	public String getStringData() {

		return null;
	}

	@Override
	public void setStringData(String stringData) {

		
	}

	@Override
	public String getSystemId() {

		return null;
	}

	@Override
	public void setSystemId(String systemId) {

		
	}

	@Override
	public String getPublicId() {

		return null;
	}

	@Override
	public void setPublicId(String publicId) {

		
	}

	@Override
	public String getBaseURI() {

		return null;
	}

	@Override
	public void setBaseURI(String baseURI) {

		
	}

	@Override
	public String getEncoding() {

		return null;
	}

	@Override
	public void setEncoding(String encoding) {

		
	}

	@Override
	public boolean getCertifiedText() {

		return false;
	}

	@Override
	public void setCertifiedText(boolean certifiedText) {

		
	}

}
