package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.report.Report;

public abstract class CurationEntity {

    static final Logger _logger = LoggerFactory.getLogger(CurationEntity.class);
    
    protected Path path = null;

    protected long size = 0;

    protected long numOfFiles = 0;

    // numOfValidFiles tells if file is valid (1) or how many valid files folder has
    protected long numOfValidFiles = 1;

    protected Collection<Report> reports = null;

    public CurationEntity(Path path) {
	this.path = path;
    }

    public CurationEntity(Path path, long size) {
	this.path = path;
	this.size = size;
    }

    public long genReport() {
	if (reports == null) {
	    reports = new LinkedList<>();
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

    public Collection<Report> getReports() {
	return reports;
    }

    public void setReports(Collection<Report> reports) {
	this.reports = reports;
    }

    public void addReport(Report report) {
	reports.add(report);
    }

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("Report for " + path).append("\n");
	sb.append("VALID: " + isValid()).append("\n");

	reports.forEach(report -> sb.append(report).append("\n"));

	return sb.toString();
    }

}
