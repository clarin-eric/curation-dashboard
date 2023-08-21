package eu.clarin.cmdi.curation.pph;

import lombok.Data;

@Data
public class ProfileHeader {
	
	private String id;
	private String schemaLocation;
	private String name;
	private String description;
	private String cmdiVersion;
	private String status;
	
	private boolean isLocalFile;
	private boolean isPublic = true;	
	private boolean isReliable = true;	
}
