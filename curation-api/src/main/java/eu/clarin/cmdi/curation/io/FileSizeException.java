package eu.clarin.cmdi.curation.io;

import org.springframework.beans.factory.annotation.Autowired;

import eu.clarin.cmdi.curation.configuration.CurationConfig;

public class FileSizeException extends Exception {

   private static final long serialVersionUID = 1L;
   private String fileName;
   private long size;
   
   @Autowired
   private CurationConfig conf;

   public FileSizeException(String fileName, long size) {
      super();
      this.fileName = fileName;
      this.size = size;
   }

   @Override
   public String getMessage() {
      return "Record " + fileName + " has size: " + size
            + " bytes but the allowed limit when processing collections is " + conf.getMaxFileSize() + " bytes.";
   }

}
