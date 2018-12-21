package eu.clarin.curation.linkchecker.httpLinkChecker;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;

import eu.clarin.curation.linkchecker.helpers.Configuration;
import eu.clarin.curation.linkchecker.urlElements.URLElement;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.mongodb.client.model.Filters.eq;

public class CollectionThread extends Thread {

    private final static Logger _logger = LoggerFactory.getLogger(CollectionThread.class);

    public ConcurrentLinkedQueue<String> urlQueue = new ConcurrentLinkedQueue<>();

    private MongoCollection<Document> linksChecked;
    private MongoCollection<Document> linksToBeChecked;
    private MongoCollection<Document> linksCheckedHistory;
    private long CRAWLDELAY;

    public CollectionThread(String name, MongoCollection<Document> linksToBeChecked, MongoCollection<Document> linksChecked, MongoCollection<Document> linksCheckedHistory, long CRAWLDELAY) {
        super(name);
        this.linksToBeChecked = linksToBeChecked;
        this.linksChecked = linksChecked;
        this.linksCheckedHistory = linksCheckedHistory;
        this.CRAWLDELAY = CRAWLDELAY;
    }

    @Override
    public void run() {

        //before starting the url check, wait 5 seconds for the queue of this thread to get populated by the httplinkchecker
        //i do this because if we don't wait and let the thread run for only one url, i'm afraid the thread
        // will be closed after one url check and it will be necessary to create a new thread for each url, which
        //is not the aim of this multithreading.
        _logger.info("Waiting 30 seconds for url queues to be filled for collection "+ getName() +"...");
        synchronized (this) {
            try {
                wait(30000);
            } catch (InterruptedException e) {
                _logger.error("Waiting for thread " + getName() + " interrupted");
            }
        }
        _logger.info("Done waiting.");

        //name of the thread is also name of the collection
        String collection = getName();

        String url;
        while ((url = urlQueue.poll()) != null) {

            HTTPLinkChecker httpLinkChecker = new HTTPLinkChecker();

            URLElement urlElement;


            try {


                urlElement = httpLinkChecker.checkLink(url, 0, 0, url);

                urlElement.setCollection(collection);
                MongoCursor<Document> cursor = linksToBeChecked.find(Filters.eq("url",url)).iterator();

                if (cursor.hasNext()) {
                    Document doc = cursor.next();
                    urlElement.setRecord(doc.get("record").toString());
                    urlElement.setExpectedMimeType(doc.get("expectedMimeType")==null?"":doc.get("expectedMimeType").toString());
                }

                //better not to have it in log file, because it makes it unreadable
//                _logger.info("Successfully checked link: "+ url);

            } catch (IOException | IllegalArgumentException e) {
                //better not to have it in log file
//                _logger.error("There is an error with the URL: " + url + " . It is not being checked.");

                e.printStackTrace();

                urlElement = new URLElement(url, null, e.getMessage() + "for URL: "+url, 0,
                        null, "0", 0, System.currentTimeMillis(), collection, 0, null,"");

                urlElement.setCollection(collection);
                MongoCursor<Document> cursor = linksToBeChecked.find(Filters.eq("url",url)).iterator();

                if (cursor.hasNext()) {
                    urlElement.setRecord(cursor.next().get("record").toString());
                }

            }

            long startTime = System.currentTimeMillis();

            //save it to the history

            Document oldElementDoc = linksChecked.find(eq("url", url)).first();
            if (oldElementDoc != null) {
                URLElement oldURLElement = new URLElement(oldElementDoc);
                linksCheckedHistory.insertOne(oldURLElement.getMongoDocument());
            }


            //replace if the url is in linksChecked already
            //if not add new
            FindOneAndReplaceOptions findOneAndReplaceOptions = new FindOneAndReplaceOptions();
            Bson filter = Filters.eq("url", urlElement.getUrl());
            linksChecked.findOneAndReplace(filter, urlElement.getMongoDocument(), findOneAndReplaceOptions.upsert(true));


            //delete from linksToBeChecked(whether successful or there was an error, ist wuascht)
            linksToBeChecked.deleteOne(eq("url", url));

            long estimatedTime = System.currentTimeMillis() - startTime;


            //I measure the time it takes to handle the mongodb operations.
            //If it takes longer than the crawldelay, we go to the next url.
            //If not, then we wait CRAWLDELAY-estimatedTime.
            //This is done, so that we don't lose
            //extra time with database operations.
            if (estimatedTime < CRAWLDELAY) {
                try {
                    sleep(CRAWLDELAY - estimatedTime);
                } catch (InterruptedException e) {
                    //do nothing, shouldnt get interrupted
                }
            }


        }


    }

}
