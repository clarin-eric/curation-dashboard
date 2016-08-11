package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDInstanceProcessor;

public class CMDInstance extends CurationEntity {

	public static Collection<String> mdSelfLinks = Collections.synchronizedCollection(new HashSet<>());
	public static Collection<String> duplicateMDSelfLink = Collections.synchronizedCollection(new HashSet<>());
	

	Collection<CMDUrlNode> links = new ArrayList<>();

	public CMDInstance(Path path) {
		super(path);
	}

	public CMDInstance(Path path, long size) {
		super(path, size);
	}

	@Override
	protected AbstractProcessor getProcessor() {
		return new CMDInstanceProcessor();
	}

	public Collection<CMDUrlNode> getLinks() {
		return links;
	}

	public void setLinks(Collection<CMDUrlNode> links) {
		this.links = links;
	}
	
	@Override
	public String toString() {
		int cnt = path.getNameCount();
		String name = path.getName(cnt - 1).toString();
		if(cnt > 1)
			name = path.getName(cnt - 2) + "/" + name;
		return "CMD Instance: " + name;
	}
}
