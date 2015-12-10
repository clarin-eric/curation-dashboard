package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.report.Message;

public abstract class CurationEntity {
	
	public static enum RecordStatPrintLvl{ALL, HEADER_ONLY, NONE};
	
	public static RecordStatPrintLvl RECORD_STAT_PRINT_LVL = RecordStatPrintLvl.ALL; 
	
	protected Path path = null;
	
	protected long size = 0;
	
	protected long numOfFiles = 0;
	
	protected boolean valid = false;
	
	Collection<Message> report = new LinkedList<Message>(){
		@Override
		public String toString() {
			if(isEmpty()) return "";
			StringBuilder sb = new StringBuilder("Report[\n" );
			this.forEach(m -> sb.append("\t" + m.toString() + "\n"));
			return sb.append("]\n").toString();
		}
	};
		
	public CurationEntity(Path path){
		this.path = path;
	}
	
	public CurationEntity(Path path, long size){
		this.path = path;
		this.size = size;
	}
			

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}
	
	public void addMessage(@Nonnull Message... message){
		for(Message m: message)
			report.add(m);
	}	
	
	public String toStat(){
		genStat();
		return getStat();
	}
	
	abstract protected String getStat();
	
	abstract protected AbstractProcessor getProcessor();
	
	public void genStat(){
		getProcessor().process(this);
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
}
