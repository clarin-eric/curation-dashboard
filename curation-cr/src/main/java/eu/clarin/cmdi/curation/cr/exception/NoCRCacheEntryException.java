package eu.clarin.cmdi.curation.cr.exception;

import java.io.Serial;

public class NoCRCacheEntryException extends Exception {

    public NoCRCacheEntryException() {
    }

    public NoCRCacheEntryException(String message) {
        super(message);
    }

    @Serial
   private static final long serialVersionUID = 1L;



}
