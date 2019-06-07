package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;


import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CollectionProcessor;

public class CMDCollection extends CurationEntity {

	Deque<CurationEntity> children;

	long numOfFiles;
	long maxFileSize = 0;
	long minFileSize = Long.MAX_VALUE;

	public CMDCollection(Path path) {
		super(path);
		children = new ArrayDeque<CurationEntity>();
	}

	@Override
	protected AbstractProcessor getProcessor() {
		return new CollectionProcessor();
	}

	public CurationEntity addChild(CurationEntity child) {
		children.add(child);

		if (child instanceof CMDInstance || child instanceof CMDProfile) {

			numOfFiles++;
			size += child.getSize();
			if (child.getSize() > maxFileSize)
				maxFileSize = child.getSize();
			if (child.getSize() < minFileSize)
				minFileSize = child.getSize();

		} else if (child instanceof CMDCollection) {
			aggregateWithDir((CMDCollection) child);
		} // else implementation for different kinds of entities if necessary

		return child;
	}

	private void aggregateWithDir(CMDCollection child) {
		numOfFiles += child.numOfFiles;
		size += child.size;
		if (child.maxFileSize > maxFileSize)
			maxFileSize = child.maxFileSize;
		if (child.minFileSize < minFileSize)
			minFileSize = child.minFileSize;
	}

	public Deque<CurationEntity> getChildren() {
		return children;
	}

	public long getNumOfFiles() {
		return numOfFiles;
	}

	public void setNumOfFiles(long numOfFiles) {
		this.numOfFiles = numOfFiles;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public long getMinFileSize() {
		return minFileSize;
	}

	public void setMinFileSize(long minFileSize) {
		this.minFileSize = minFileSize;
	}
	
	@Override
	public String toString() {
		return "Collection: " + path.toString();
	}
}
