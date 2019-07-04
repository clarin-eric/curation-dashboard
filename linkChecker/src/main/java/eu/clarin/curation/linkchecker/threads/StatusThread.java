package eu.clarin.curation.linkchecker.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//A _logger thread that outputs every 10 seconds the current state of Collection threads...
public class StatusThread extends Thread {

    private final static Logger _logger = LoggerFactory.getLogger(StatusThread.class);

    @Override
    public void run() {


        while (true) {
            //log current state
            int i = 0;
            for (Thread tr : Thread.getAllStackTraces().keySet()) {
                if (tr.getClass().equals(CollectionThread.class)) {
                    i++;
                    _logger.info("Collection thread: " + tr.getName() + " is running, with " + ((CollectionThread) tr).urlQueue.size() + " links in its queue.");
                }

            }
            _logger.info("Currently, there are " + i + " collection threads running...");

            synchronized (this) {
                try {
                    wait(10000);
                } catch (InterruptedException e) {
                    //dont do anything, this thread is not that important.
                }
            }
        }

    }
}
