package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.DirectoryProcessor;

public class Directory extends CurationEntity {

    private Collection<CurationEntity> children;

    private long maxFileSize = 0;
    private long minFileSize = Long.MAX_VALUE;


    public Directory(Path path) {
	super(path);
	children = new LinkedList<CurationEntity>();
	numOfValidFiles = 0;
    }

    @Override
    protected AbstractProcessor getProcessor() {
	return new DirectoryProcessor();
    }

    public CurationEntity addChild(CurationEntity child) {
	children.add(child);

	if (child instanceof CMDIRecord || child instanceof CMDIProfile) {

	    numOfFiles++;
	    size += child.getSize();
	    if (child.getSize() > maxFileSize)
		maxFileSize = child.getSize();
	    if (child.getSize() < minFileSize)
		minFileSize = child.getSize();

	} else if (child instanceof Directory) {
	    aggregateWithDir((Directory) child);
	} else {
	    // implementation for different kinds of entities if necessary
	}

	return child;
    }
    
    private void aggregateWithDir(Directory child) {
	numOfFiles += child.numOfFiles;
	numOfValidFiles += child.numOfValidFiles;
	size += child.size;
	if (child.maxFileSize > maxFileSize)
	    maxFileSize = child.maxFileSize;
	if (child.minFileSize < minFileSize)
	    minFileSize = child.minFileSize;
    }

    public Collection<CurationEntity> getChildren() {
	return children;
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

}
