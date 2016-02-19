package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.report.Report;

public abstract class CurationEntity {

    static final Logger _logger = LoggerFactory.getLogger(CurationEntity.class);
    
    
    protected Path path = null;
    
    protected Report report = null;

    protected long size = 0;

    // validity -> negative - not set; 0 - not valid; positive - number of valid file in case of directory
    protected long validity = -1;    

    public CurationEntity(Path path) {
	this.path = path;
    }

    public CurationEntity(Path path, long size) {
	this.path = path;
	this.size = size;
    }
    
    public Report generateReport(){
	if(report == null)
	    report = getProcessor().process(this);
	return report;
    }

    protected abstract AbstractProcessor getProcessor();

    public Path getPath() {
	return path;
    }

    public void setPath(Path path) {
	this.path = path;
    }

    public long getValidity() {
	return validity;
    }

    public void setValidity(long validity) {
	this.validity = validity;
    }

    public boolean isValid() {
	return validity > 0;
    }

    public long getSize() {
	return size;
    }

    public void setSize(long size) {
	this.size = size;
    }
    
    public Report getReport(){
	return report;
    }  

    

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Report for " + path).append("\n");
	sb.append("VALID: " + isValid()).append("\n");
	

	return sb.toString();
    }

}
