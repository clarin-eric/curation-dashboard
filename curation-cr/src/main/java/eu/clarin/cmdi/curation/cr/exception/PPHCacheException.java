package eu.clarin.cmdi.curation.cr.exception;

import java.io.Serial;

public class PPHCacheException extends Exception {

   @Serial
   private static final long serialVersionUID = 1L;

   public PPHCacheException(Throwable cause) {
      super(cause);
   }

}
