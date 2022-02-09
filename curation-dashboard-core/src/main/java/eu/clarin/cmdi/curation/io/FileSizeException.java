package eu.clarin.cmdi.curation.io;

import eu.clarin.cmdi.curation.main.Configuration;

public class FileSizeException extends Exception{
	
	private static final long serialVersionUID = 1L;
    private String fileName;
	private long size;
	
	public FileSizeException(String fileName, long size) {
		super();
		this.fileName = fileName;
		this.size = size;
	}

	@Override
	public String getMessage() {
		return "Record " + fileName + " has size: " + size + " bytes but the allowed limit when processing collections is " + Configuration.MAX_FILE_SIZE + " bytes.";
	}

}
