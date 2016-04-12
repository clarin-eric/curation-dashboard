package eu.clarin.cmdi.curation.cr;

import java.util.List;

import javax.xml.validation.Schema;

import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.cr.ProfileDescriptions.ProfileHeader;

public interface ICRService {
	
	public boolean isPublic(final String profileId) throws Exception;
	
	public List<ProfileHeader> getPublicProfiles() throws Exception;
	
	public ProfileHeader getProfileHeader(final String profileId) throws Exception;
	
	public boolean isNameUnique(String name) throws Exception;
	
	public boolean isDescriptionUnique(String description) throws Exception;
	
	public boolean isSchemaCRResident(String schemaUrl);	
	
	public Schema getSchema(final String profileId) throws Exception;
		
	public VTDNav getParsedXSD(final String profileId) throws Exception;
	
	public VTDNav getParsedXML(final String profileId) throws Exception;
	
	public double getScore(final String profileId) throws Exception;
}
