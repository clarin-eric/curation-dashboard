package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.DirectoryProcessor;

public class Directory extends CurationEntity{
	
	private Collection<CurationEntity> children;
	
	private long size = 0;
	private long numOfValidFiles = 0;
	private long maxFileSize = 0;
	private long minFileSize = Long.MAX_VALUE;
	
	
	
	
	public Directory(Path path) {
		super(path);
		children = new LinkedList<CurationEntity>();
	}

	public CurationEntity addChild(CurationEntity child){
		children.add(child);
		
		if(child instanceof CMDIRecord){
			CMDIRecord cmdiChild = (CMDIRecord)child;
			
			numOfFiles++;
			size += cmdiChild.getSize();
			if(cmdiChild.getSize() > maxFileSize)
				maxFileSize = cmdiChild.getSize();
			if(cmdiChild.getSize() < minFileSize)
				minFileSize = cmdiChild.getSize();
			
		}else if(child instanceof Directory){
			Directory dirChild = (Directory) child;
			numOfFiles += dirChild.numOfFiles;
			size += dirChild.size;
			if(dirChild.maxFileSize > maxFileSize)
				maxFileSize = dirChild.maxFileSize;
			if(dirChild.minFileSize < minFileSize)
				minFileSize = dirChild.minFileSize;
		}else{
			//implementation for different kinds of entities if necessary
		}
		
		
		return child;
	}
		
	@Override
	public String getStat() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("Colection: " + path.getFileName() + "\n");
		sb.append("Total number of records: " + numOfFiles + "\n");
		sb.append("Total number of valid records: " + numOfValidFiles + "\n");
		sb.append("Average number of valid records: " + new DecimalFormat("0%").format(div(numOfValidFiles,numOfFiles)) + "\n");
		sb.append("Total file size: " + size + " bytes" + "\n");
		sb.append("Average file size: " + new DecimalFormat("0.00").format(div(size, numOfFiles)) + " bytes" + "\n");
		sb.append("max/min file size: " + maxFileSize + "/" + minFileSize  + " bytes" + "\n");
		sb.append("\n");		
		
		children.forEach(child -> sb.append(child.getStat()));		
		
		return sb.toString();		
	}
	
	private double div(double a, double b){
		return a/b;
	}
	
	@Override
	protected AbstractProcessor getProcessor() {
		return new DirectoryProcessor();
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

	public long getNumOfValidFiles() {
		return numOfValidFiles;
	}

	public void setNumOfValidFiles(long numOfValidFiles) {
		this.numOfValidFiles = numOfValidFiles;
	}
	
	
	
}
