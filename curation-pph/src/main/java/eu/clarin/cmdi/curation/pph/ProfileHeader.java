package eu.clarin.cmdi.curation.pph;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
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
