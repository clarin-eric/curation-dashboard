package eu.clarin.curation.linkchecker.threads;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import eu.clarin.curation.linkchecker.helpers.Configuration;
import eu.clarin.curation.linkchecker.urlElements.URLElement;
import eu.clarin.curation.linkchecker.urlElements.URLElementToBeChecked;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mongodb.client.model.Filters.eq;
import static eu.clarin.curation.linkchecker.helpers.Configuration.DATABASE;

//manage all the threads
//also calculate average values:
//  url per minute
//  url per hour
//  url per day
//...
public class CollectionThreadManager {


    private final static Logger _logger = LoggerFactory.getLogger(CollectionThreadManager.class);

    MongoCollection<Document> linksToBeChecked = DATABASE.getCollection("linksToBeChecked");

    MongoCollection<Document> linksChecked = DATABASE.getCollection("linksChecked");

    MongoCollection<Document> linksCheckedHistory = DATABASE.getCollection("linksCheckedHistory");

    public void start() {

        //outputs currently running collection threads
        new StatusThread().start();

        //starts all collection threads based on linksToBeChecked
        startCollectionThreads();

        //every 24 hours
        //todo scheduler every 24 hours
        refillCollectionThreads();

    }

    //if the collectionThread is not running, it means it is finished.
    //So we copy all urls of this collection from linksChecked back to linksToBeChecked
    //So every 24 hours we restart finished collection threads.
    private void refillCollectionThreads() {

        for (String collection : linksChecked.distinct("collection", String.class)) {

            CollectionThread collectionThread = getCollectionThreadByName(collection);

            //if collection thread isn't running, copy all links back to linksToBeChecked
            if (collectionThread == null) {
                refillLinksToBeChecked(collection);
                CollectionThread t = startCollectionThread(collection);


                try (MongoCursor<Document> cursor = linksToBeChecked.find(eq("collection",collection)).noCursorTimeout(true).iterator()) {

                    while (cursor.hasNext()) {
                        URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(cursor.next());

                        if (!t.urlQueue.contains(urlElementToBeChecked)) {
                            t.urlQueue.add(urlElementToBeChecked);
//                            _logger.info("Added url to collection thread: " + collection);
                        }

                    }

                    _logger.info("Successfully restarted collection thread: "+collection);


                } catch (MongoException e) {
                    _logger.error("Mongo error: ", e);
                }

            }

        }


    }

    private void refillLinksToBeChecked(String collection) {

        for (Document document : linksChecked.find(eq("collection", collection))) {

            URLElement urlElement = new URLElement(document);
            String url = urlElement.getUrl();

            URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(url, urlElement.getRecord(), urlElement.getCollection(), urlElement.getExpectedMimeType());
            try {
                linksToBeChecked.insertOne(urlElementToBeChecked.getMongoDocument());
            } catch (MongoException e) {
                //duplicate key error
                //url is already in the database, do nothing
            }

        }

    }

    private CollectionThread startCollectionThread(String collection) {
        CollectionThread t;
        //handle specific crawl delay if any
        long crawlDelay;
        if (Configuration.CRAWLDELAYMAP.containsKey(collection)) {
            crawlDelay = Configuration.CRAWLDELAYMAP.get(collection);
            _logger.info("Crawl delay set to: " + crawlDelay + "for collection " + collection);
        } else {
            //should be 0
            crawlDelay = Configuration.CRAWLDELAY;
        }

        t = new CollectionThread(collection, linksToBeChecked, linksChecked, linksCheckedHistory, crawlDelay);
        t.start();

        _logger.info("Started collection thread: " + collection);

        return t;
    }


    private void startCollectionThreads() {

        if (Configuration.ONLY_BROKEN) {
            _logger.info("Putting broken links back into linksToBeChecked");
            Bson brokenLinksFilter = Filters.not(Filters.in("status", 200, 302, 401, 405, 429));
            MongoCursor<Document> cursor = linksChecked.find(brokenLinksFilter).noCursorTimeout(true).iterator();

            int i = 0;
            try {
                while (cursor.hasNext()) {
                    URLElement urlElement = new URLElement(cursor.next());
                    URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(urlElement.getUrl(), urlElement.getRecord(), urlElement.getCollection(), urlElement.getExpectedMimeType());

                    try {
                        linksToBeChecked.insertOne(urlElementToBeChecked.getMongoDocument());
                    } catch (MongoException e) {
                        //duplicate key error
                        //url is already in the database, do nothing
                    }

                    if (i % 100 == 0) {
                        _logger.info("Added " + i + " broken urls to linksToBeChecked from linksChecked.");
                    }
                    i++;

                }
            } finally {
                cursor.close();
            }

        }

        try (MongoCursor<Document> cursor = linksToBeChecked.find().sort(Sorts.ascending("collection")).noCursorTimeout(true).iterator()) {
            _logger.info("LinksToBeChecked document count: " + linksToBeChecked.countDocuments());
            while (cursor.hasNext()) {
                URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(cursor.next());

                String collection = urlElementToBeChecked.getCollection();

//                    _logger.info("URL to be checked: " + url + ", from collection: " + collection);

                CollectionThread t = getCollectionThreadByName(collection);
                //start new thread if it doesn't exist already
                if (t == null) {
                    t = startCollectionThread(collection);
                }

                if (!t.urlQueue.contains(urlElementToBeChecked)) {
                    t.urlQueue.add(urlElementToBeChecked);
                    _logger.info("Added url to collection thread: " + collection);
                }

            }

            _logger.info("Added all links to respective threads.");


        } catch (MongoException e) {
            _logger.error("Mongo error: ", e);
        }


    }

    private static CollectionThread getCollectionThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName) && t.getClass().equals(CollectionThread.class)) {
                return (CollectionThread) t;
            }
        }
        return null;
    }
}
