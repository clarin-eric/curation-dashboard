package eu.clarin.cmdi.curation.cr;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;

@XmlRootElement(name="profileDescriptions")
@XmlAccessorType(XmlAccessType.FIELD)
public class PublicProfiles {
	
	private Collection<ProfileHeader> profileDescription;
	
	
	public static Collection<ProfileHeader> createPublicProfiles(){
		try{
			XMLMarshaller<PublicProfiles> publicProfilesMarshaller = new XMLMarshaller<>(PublicProfiles.class);

			Collection<ProfileHeader> publicProfiles = null;
			
			try(InputStream in = new URL(Configuration.VLO_CONFIG.getComponentRegistryRESTURL() + "?" + Configuration.CR_QUERY).openStream()){
			   publicProfiles = publicProfilesMarshaller
			      .unmarshal(in)
					.profileDescription;
			
   			publicProfiles.forEach(p -> {
   				p.setCmdiVersion("1.x");
   				p.setSchemaLocation(Configuration.VLO_CONFIG.getComponentRegistryProfileSchema(p.getId()));
   			});
			}
			
			return publicProfiles;
			
		}
		catch(Exception e){
			throw new RuntimeException("Unable to read xml from " + Configuration.VLO_CONFIG.getComponentRegistryRESTURL() + ", CLARIN Component Registry is unavailable! Please try later", e);
		}		
	}
}
