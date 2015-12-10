package eu.clarin.cmdi.curation.component_registry;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "profileDescriptions")
public class CMDIProfiles {
	
		
	@XmlElement(name = "profileDescription")
	List<ProfileDescription> profiles;	
	
	public List<ProfileDescription> getProfiles() {
		return profiles;
	}
		
	//child node ProfileDescription
	
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ProfileDescription{
		
		@XmlElement(name = "id")
		String id;
		
		@XmlElement(name = "name")
		String name;
		
		@XmlElement(name = "description")
		String description;
		

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}
		
	}
 }
