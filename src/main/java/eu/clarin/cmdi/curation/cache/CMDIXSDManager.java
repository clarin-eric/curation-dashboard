package eu.clarin.cmdi.curation.cache;

import java.nio.file.Path;

import org.apache.commons.jcs.access.CacheAccess;

public class CMDIXSDManager {
	
	private static CMDIXSDManager singlton;
	
	private static CacheAccess<String, Path> cache;
	
	private CMDIXSDManager(){
		
	}
	
	
	public static CMDIXSDManager getInstance(){
		synchronized (CMDIXSDManager.class) {
			if(singlton == null)
				singlton = new CMDIXSDManager();			
		}
		
		return singlton;
		
		
	}

}
