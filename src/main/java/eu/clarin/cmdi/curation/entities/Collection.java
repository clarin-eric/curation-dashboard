package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CollectionProcessor;

public class Collection extends CurationEntity {

    List<CurationEntity> children;

    long numOfFiles;
    long maxFileSize = 0;
    long minFileSize = Long.MAX_VALUE;
    
    public Collection(Path path) {
	super(path);
	children = new LinkedList<CurationEntity>();
	validity = 0;
    }

    @Override
    protected AbstractProcessor getProcessor() {
	return new CollectionProcessor();
    }

    public CurationEntity addChild(CurationEntity child) {
	children.add(child);

	if (child instanceof CMDIInstance || child instanceof CMDIProfile) {

	    numOfFiles++;
	    size += child.getSize();
	    if (child.getSize() > maxFileSize)
		maxFileSize = child.getSize();
	    if (child.getSize() < minFileSize)
		minFileSize = child.getSize();

	} else if (child instanceof Collection) {
	    aggregateWithDir((Collection) child);
	} //else implementation for different kinds of entities if necessary
	

	return child;
    }
    
    private void aggregateWithDir(Collection child) {
	numOfFiles += child.numOfFiles;
	validity += child.validity;
	size += child.size;
	if (child.maxFileSize > maxFileSize)
	    maxFileSize = child.maxFileSize;
	if (child.minFileSize < minFileSize)
	    minFileSize = child.minFileSize;
    }

    public List<CurationEntity> getChildren() {
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
}
