package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import eu.clarin.cmdi.curation.component_registry.ComponentRegistryService;
import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDIProcessor;

public class CMDIRecord extends CurationEntity{	

	public final static Set<String> uniqueMDSelfLinks = Collections.synchronizedSet(new HashSet<>());
	public final static Collection<String> duplicateMDSelfLinks = Collections.synchronizedCollection(new LinkedList<>());
	
	private String profile = null; //is stored in digits, url, and profile format are converted in setProfile	
	
	Collection<String> values;
	
	public CMDIRecord(Path path) {
		super(path);
	}
	
	public CMDIRecord(Path path, long size){
		super(path, size);
	}
	
	@Override
	protected AbstractProcessor getProcessor() {
		return new CMDIProcessor();
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

	public Collection<String> getValues() {
		return values;
	}

	public void setValues(Collection<String> values) {
		this.values = values;
	}
	

}
