package eu.clarin.cmdi.curation.cr.exception;

import java.io.Serial;

public class ProfileVersionNotSupportedException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProfileVersionNotSupportedException() {
    }

    public ProfileVersionNotSupportedException(String message) {
        super(message);
    }
}
