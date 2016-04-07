package eu.clarin.cmdi.curation.cr;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileDescriptions {

	List<ProfileHeader> profileDescription;

	@XmlRootElement()
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ProfileHeader {
		String id;
		String name;
		String description;
		
		transient boolean isPublic;

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}
		
		public boolean isPublic(){
			return isPublic;
		}
	}

}
