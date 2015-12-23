package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.report.Report;

public abstract class CurationEntity {
	
	public static enum RecordStatPrintLvl{ALL, HEADER_ONLY, NONE};
	
	public static RecordStatPrintLvl RECORD_STAT_PRINT_LVL = RecordStatPrintLvl.ALL; 
	
	protected Path path = null;
	
	protected long size = 0;
	
	protected long numOfFiles = 0;
	
	protected boolean valid = false;
	
	protected Collection<Report> reports = new LinkedList<Report>();
	
		
	public CurationEntity(Path path){
		this.path = path;
	}
	
	public CurationEntity(Path path, long size){
		this.path = path;
		this.size = size;
	}
	
	public long genReport(){
		getProcessor().process(this);
		return isValid()? 1l : 0l;
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
	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public long getSize(){
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
	
	public void addReport(Report report){
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
