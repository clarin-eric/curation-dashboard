package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.report.Detail;
import eu.clarin.cmdi.curation.report.Severity;

public abstract class CurationEntity {

    static final Logger _logger = LoggerFactory.getLogger(CurationEntity.class);
    
    protected Path path = null;

    protected long size = 0;

    protected long numOfFiles = 0;

    // numOfValidFiles tells if file is valid (1) or how many valid files folder has
    protected long numOfValidFiles = -1;

    protected Collection<Detail> details = null;

    public CurationEntity(Path path) {
	this.path = path;
    }

    public CurationEntity(Path path, long size) {
	this.path = path;
	this.size = size;
    }

    public long genReport() {
	if (numOfValidFiles < 0) {
	    getProcessor().process(this);
	}
	
	return numOfValidFiles;
    }

    protected abstract AbstractProcessor getProcessor();

    public Path getPath() {
	return path;
    }

    public void setPath(Path path) {
	this.path = path;
    }

    public long getNumOfFiles() {
	return numOfFiles;
    }

    public void setNumOfFiles(long numOfFiles) {
	this.numOfFiles = numOfFiles;
    }

    public long getNumOfValidFiles() {
	return numOfValidFiles;
    }

    public void setNumOfValidFiles(long numOfValidFiles) {
	this.numOfValidFiles = numOfValidFiles;
    }

    public boolean isValid() {
	return genReport() > 0;
    }

    public long getSize() {
	return size;
    }

    public void setSize(long size) {
	this.size = size;
    }

    public Collection<Detail> getDetails(){
	return details;
    }
    
    public void addDetail(Severity lvl, String message){
	details.add(new Detail(lvl, message));
    }
    

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Report for " + path).append("\n");
	sb.append("VALID: " + isValid()).append("\n");

	details.forEach(detail -> sb.append(detail).append("\n"));

	return sb.toString();
    }

}
