package eu.clarin.cmdi.curation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static Logger LOG = LoggerFactory.getLogger(UncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();//so that the caller gets the stack trace
        LOG.error("Uncaught exception: ", e);//so that the logger gets the stack trace
    }
}
