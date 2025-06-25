package eu.clarin.cmdi.curation.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Only CMDI 1.2 is supported")
public class ProfileNotSupportedException extends RuntimeException {
}
