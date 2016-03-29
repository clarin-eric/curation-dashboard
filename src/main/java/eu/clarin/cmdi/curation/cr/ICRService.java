package eu.clarin.cmdi.curation.cr;

import javax.xml.validation.Schema;

import com.ximpleware.VTDNav;

public interface ICRService {
	
	public boolean isPublic(final String profileId) throws Exception;
	
	public boolean isSchemaCRResident(String schemaUrl);	
	
	public Schema getSchema(final String profileId) throws Exception;
		
	public VTDNav getParsedXSD(final String profileId) throws Exception;
	
	public VTDNav getParseXML(final String profileId) throws Exception;
	
	public double getScore(final String profileId) throws Exception;



}
