package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDInstanceProcessor;

public class CMDInstance extends CurationEntity {

	public static Set<String> uniqueMDSelfLinks = Collections.synchronizedSet(new HashSet<>());
	public static Collection<String> duplicateMDSelfLinks = Collections.synchronizedCollection(new LinkedList<>());

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

	public static Set<String> getUniquemdselflinks() {
		return uniqueMDSelfLinks;
	}

	public static Collection<String> getDuplicatemdselflinks() {
		return duplicateMDSelfLinks;
	}

}
