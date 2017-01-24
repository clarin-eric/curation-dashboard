package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import eu.clarin.cmdi.curation.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDInstanceProcessor;

public class CMDInstance extends CurationEntity {

	public static Collection<String> mdSelfLinks = Collections.synchronizedCollection(new HashSet<>());
	public static Collection<String> duplicateMDSelfLink = Collections.synchronizedCollection(new HashSet<>());
	
	private ParsedInstance parsedInstance = null;

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
	
	public ParsedInstance getParsedInstance() {
		return parsedInstance;
	}

	public void setParsedInstance(ParsedInstance parsedInstance) {
		this.parsedInstance = parsedInstance;
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
