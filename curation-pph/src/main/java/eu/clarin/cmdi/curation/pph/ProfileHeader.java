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
	
	private transient boolean isLocalFile;
	private transient boolean isPublic = true;
	
}
