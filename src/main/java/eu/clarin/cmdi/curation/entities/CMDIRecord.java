package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import eu.clarin.cmdi.curation.component_registry.ComponentRegistryService;
import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDIProcessor;

public class CMDIRecord extends CurationEntity{	

	public final static Collection<String> duplicateMDSelfLinks = Collections.synchronizedCollection(new LinkedList<>());
		
	private int numOfElements = 0;
	private int numOfEmptyElements = 0;	
	private int numOfResourceProxies = 0;
	
	private String profile = null; //is stored in digits, url, and profile format are converted in setProfile
	private String selfLink = null;
	private String collectionDisplayName = null;
	
	
	
	public CMDIRecord(Path path) {
		super(path);
	}
	
	public CMDIRecord(Path path, long size){
		super(path, size);
	}

	@Override
	public String getStat() {
		StringBuilder sb = new StringBuilder("");
		switch(RECORD_STAT_PRINT_LVL){		
			case ALL:
			case HEADER_ONLY:
				sb.append("CMDI Record: " + path.getFileName() + "\n");
				sb.append("size: " + size + " bytes" + "\n");
				sb.append("valid: " + valid + "\n");
				if(RECORD_STAT_PRINT_LVL == RecordStatPrintLvl.HEADER_ONLY)
					break;				
				sb.append(report);				
			case NONE:			
		}	
		
		return sb.toString();
				
	}

	@Override
	protected AbstractProcessor getProcessor() {
		return new CMDIProcessor();
	}

	public int getNumOfElements() {
		return numOfElements;
	}

	public void setNumOfElements(int numOfElements) {
		this.numOfElements = numOfElements;
	}

	public int getNumOfEmptyElements() {
		return numOfEmptyElements;
	}

	public void setNumOfEmptyElements(int numOfEmptyElements) {
		this.numOfEmptyElements = numOfEmptyElements;
	}

	public int getNumOfResourceProxies() {
		return numOfResourceProxies;
	}

	public void setNumOfResourceProxies(int numOfResourceProxies) {
		this.numOfResourceProxies = numOfResourceProxies;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		if(profile.startsWith(ComponentRegistryService.CLARIN_COMPONENT_REGISTRY_REST_URL))//url
			this.profile = profile.substring((ComponentRegistryService.CLARIN_COMPONENT_REGISTRY_REST_URL + ComponentRegistryService.PROFILE_PREFIX).length(), profile.indexOf("/xsd"));
		else if (profile.startsWith(ComponentRegistryService.PROFILE_PREFIX))//profile format: clarin.eu:cr1:p_xxx
			this.profile = profile.substring(ComponentRegistryService.PROFILE_PREFIX.length());
		else//just digits
			this.profile = profile;
	}

	public String getSelfLink() {
		return selfLink;
	}

	public void setSelfLink(String selfLink) {
		this.selfLink = selfLink;
	}

	public String getCollectionDisplayName() {
		return collectionDisplayName;
	}

	public void setCollectionDisplayName(String collectionDisplayName) {
		this.collectionDisplayName = collectionDisplayName;
	}
	

}
