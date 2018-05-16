package eu.clarin.cmdi.curation.facets.postprocessor.utils;

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class VLOConfigFactory {
	private static final String VLO_CONFIG_URL = "https://raw.githubusercontent.com/clarin-eric/VLO/88115eeb1908a356d0f74885ed48ec2d4a2f4a4c/vlo-commons/src/main/resources/VloConfig.xml";
//	private static final String VLO_CONFIG_URL = "https://raw.githubusercontent.com/clarin-eric/VLO/master/vlo-commons/src/main/resources/VloConfig.xml";
	private static VloConfig config = null;
	
	public static synchronized VloConfig getVloConfig(){
		if(config == null){
			try{
				JAXBContext context = JAXBContext.newInstance(VloConfig.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				config = (VloConfig) unmarshaller.unmarshal(new URL(VLO_CONFIG_URL));
			}catch(Exception e){
				throw new RuntimeException("Unabele to create vlo config from " + VLO_CONFIG_URL, e);
			}
		}
		
		return config;
	}

}
