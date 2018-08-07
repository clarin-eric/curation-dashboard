package eu.clarin.curation.linkchecker.httpLinkChecker;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;

import eu.clarin.curation.linkchecker.urlElements.URLElement;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.mongodb.client.model.Filters.eq;

public class CollectionThread extends Thread {

    private final static Logger logger = LoggerFactory.getLogger(CollectionThread.class);

    public ConcurrentLinkedQueue<String> urlQueue = new ConcurrentLinkedQueue<>();

    private MongoCollection<Document> linksChecked;
    private MongoCollection<Document> linksToBeChecked;

    public CollectionThread(String name, MongoCollection<Document> linksToBeChecked, MongoCollection<Document> linksChecked) {
        super(name);
        this.linksToBeChecked = linksToBeChecked;
        this.linksChecked = linksChecked;
    }


    @Override
    public void run() {

        //before starting the url check, wait 5 seconds for the queue of this thread to get populated by the httplinkchecker
        //i do this because if we don't wait and let the thread run for only one url, i'm afraid the thread
        // will be closed after one url check and it will be necessary to create a new thread for each url, which
        //is not the aim of this multithreading.
        logger.info("waiting...");
        synchronized (this) {
            try {
                wait(5000);
            } catch (InterruptedException e) {
                logger.error("Waiting for thread " + getName() + " interrupted");
            }
        }
        logger.info("done waiting.");

        //name of the thread is also name of the collection
        String collection = getName();

        String url;
        while ((url = urlQueue.poll()) != null) {

            HTTPLinkChecker httpLinkChecker = new HTTPLinkChecker();

            URLElement urlElement;

            if (url == null) {
                logger.error("The URL is null: " + url + " .");
            } else {
                try {

                    urlElement = httpLinkChecker.checkLink(url, 0, 0, url);
                    urlElement.setCollection(collection);


                } catch (IOException | IllegalArgumentException e) {
                    logger.error("There is an error with the URL: " + url + " . It is not being checked.");

                    urlElement = new URLElement(url, null, e.getLocalizedMessage(), 0,
                            null, "0", 0, System.currentTimeMillis(), collection, 0);

                }
                //replace if the url is in linksChecked already
                //if not add new
                FindOneAndReplaceOptions findOneAndReplaceOptions = new FindOneAndReplaceOptions();
                Bson filter = Filters.eq("url", urlElement.getUrl());
                linksChecked.findOneAndReplace(filter, urlElement.getMongoDocument(), findOneAndReplaceOptions.upsert(true));

            }

            //delete from linksToBeChecked(whether successful or there was an error, ist wuascht)
            linksToBeChecked.deleteOne(eq("url", url));
            logger.info("done checking url.");

        }


    }

}
