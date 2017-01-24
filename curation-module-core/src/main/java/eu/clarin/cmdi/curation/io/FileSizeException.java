package eu.clarin.cmdi.curation.io;

import eu.clarin.cmdi.curation.main.Configuration;

public class FileSizeException extends Exception{
	
	private String fileName;
	private long size;
	

	
	public FileSizeException(String fileName, long size) {
		super();
		this.fileName = fileName;
		this.size = size;
	}



	@Override
	public String getMessage() {
		return "Record " + fileName + " has size: " + size + "B but the allowed limit when processing collections is " + Configuration.MAX_FILE_SIZE + "B";
	}

}
