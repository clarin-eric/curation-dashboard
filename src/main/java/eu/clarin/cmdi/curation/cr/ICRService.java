package eu.clarin.cmdi.curation.cr;

import java.net.URL;

import javax.xml.validation.Schema;

import com.ximpleware.VTDNav;

public interface ICRService {
	
	public boolean isPublic(final String profileId) throws Exception;
	
	public boolean isSchemaCRResident(final URL schemaUrl);
	
	public Schema getSchema(final String profileId) throws Exception;
		
	public VTDNav getParsedXSD(final String profileId) throws Exception;
	
	public VTDNav getParseXML(final String profileId) throws Exception;
	
	public double getScore(final String profileId) throws Exception;	

}
