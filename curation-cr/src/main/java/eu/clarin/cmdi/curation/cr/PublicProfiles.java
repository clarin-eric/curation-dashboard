package eu.clarin.cmdi.curation.cr;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.xml.XMLMarshaller;
import lombok.extern.slf4j.Slf4j;

@XmlRootElement(name="profileDescriptions")
@XmlAccessorType(XmlAccessType.FIELD)
@Slf4j
public class PublicProfiles {
	
	private Collection<ProfileHeader> profileDescription;	
	
	
	public static Collection<ProfileHeader> createPublicProfiles(String schemaRestUrl, String query){
		
	   String cr = null;
	   
	   try{
			XMLMarshaller<PublicProfiles> publicProfilesMarshaller = new XMLMarshaller<>(PublicProfiles.class);

			Collection<ProfileHeader> publicProfiles = null;
			
			cr = schemaRestUrl + query;
			log.trace("component registry URL: {}", cr);
			
			try(InputStream in = new URL(cr).openStream()){
			   publicProfiles = publicProfilesMarshaller
			      .unmarshal(in)
					.profileDescription;
			
   			publicProfiles.forEach(p -> {
   				p.setCmdiVersion("1.x");
   				p.setSchemaLocation(schemaRestUrl + "/" + p.getId() + "/xsd");
   			});
			}
			
			return publicProfiles;
			
		}
		catch(Exception e){
			throw new RuntimeException("Unable to read xml from " + cr + ", CLARIN Component Registry is unavailable! Please try later", e);
		}		
	}
}
