package eu.clarin.cmdi.curation.cr;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.beans.factory.annotation.Autowired;

import eu.clarin.cmdi.curation.cr.conf.CRProperties;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;
import eu.clarin.cmdi.vlo.config.VloConfig;
import lombok.extern.slf4j.Slf4j;

@XmlRootElement(name="profileDescriptions")
@XmlAccessorType(XmlAccessType.FIELD)
@Slf4j
public class PublicProfiles {
	
	private Collection<ProfileHeader> profileDescription;
	
	@Autowired
	private static VloConfig vloConf;
	@Autowired
	private static CRProperties crProps;
	
	
	public static Collection<ProfileHeader> createPublicProfiles(){
		
	   String cr = null;
	   
	   try{
			XMLMarshaller<PublicProfiles> publicProfilesMarshaller = new XMLMarshaller<>(PublicProfiles.class);

			Collection<ProfileHeader> publicProfiles = null;
			
			cr = vloConf.getComponentRegistryRESTURL() + "?" + crProps.getCrQuery();
			log.trace("component registry URL: {}", cr);
			
			try(InputStream in = new URL(cr).openStream()){
			   publicProfiles = publicProfilesMarshaller
			      .unmarshal(in)
					.profileDescription;
			
   			publicProfiles.forEach(p -> {
   				p.setCmdiVersion("1.x");
   				p.setSchemaLocation(vloConf.getComponentRegistryProfileSchema(p.getId()));
   			});
			}
			
			return publicProfiles;
			
		}
		catch(Exception e){
			throw new RuntimeException("Unable to read xml from " + cr + ", CLARIN Component Registry is unavailable! Please try later", e);
		}		
	}
}
