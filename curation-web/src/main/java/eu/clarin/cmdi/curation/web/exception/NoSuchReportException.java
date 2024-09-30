package eu.clarin.cmdi.curation.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Report doesn't exist")
public class NoSuchReportException extends RuntimeException {
}
