package eu.clarin.curation.linkchecker;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import eu.clarin.curation.linkchecker.helpers.Configuration;
import eu.clarin.curation.linkchecker.httpLinkChecker.CollectionThread;
import eu.clarin.curation.linkchecker.urlElements.URLElement;
import eu.clarin.curation.linkchecker.urlElements.URLElementToBeChecked;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class Main {

    private final static Logger _logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws ParseException {
        
     // create Options object
        Options options = new Options();

        // add t option
        options.addOption(Option.builder("config")
                .required(true)
                .hasArg(true)
                .build());
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        if (!cmd.hasOption("config")) {
            _logger.info("Usage: Please provide the config file path as a parameter.");
            System.exit(1);
        }

        Configuration.loadConfigVariables(cmd.getOptionValue("config"));

        //connect to mongod and get database
        MongoDatabase database = getMongoDatabase();

        //get links from linksToBeChecked
        MongoCollection<Document> linksToBeChecked = database.getCollection("linksToBeChecked");

        //get linksChecked
        MongoCollection<Document> linksChecked = database.getCollection("linksChecked");

        //Ensure that "url" is a unique index
        IndexOptions indexOptions = new IndexOptions().unique(true);
        linksChecked.createIndex(new Document("url", 1), indexOptions);
        linksToBeChecked.createIndex(new Document("url", 1), indexOptions);

        while (true) {


            MongoCursor<Document> cursor = linksToBeChecked.find().iterator();
            try {
                while (cursor.hasNext()) {
                    URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(cursor.next());

                    String collection = urlElementToBeChecked.getCollection();
                    String url = urlElementToBeChecked.getUrl();
                    _logger.info("URL to be checked: " + url + ", from collection: " + collection);


                    CollectionThread t = getCollectionThreadByName(collection);
                    if (t == null) {
                        t = new CollectionThread(collection, linksToBeChecked, linksChecked, Configuration.CRAWLDELAY);
                        t.urlQueue.add(url);

                        t.start();
                    } else {
                        if (!t.urlQueue.contains(url)) {
                            t.urlQueue.add(url);
                        }
                    }

                }

                _logger.info("Added all links to respective threads.");


                //Create a _logger thread that outputs every 10 seconds the current state of Collection threads...
                (new Thread() {
                    public void run() {

                        while (true) {
                            //log current state
                            for (Thread tr : Thread.getAllStackTraces().keySet()) {
                                if (tr.getClass().equals(CollectionThread.class)) {
                                    _logger.info("Collection thread: " + tr.getName() + " is running.");
                                    _logger.info("It has " + ((CollectionThread) tr).urlQueue.size() + " links in its queue.");
                                }
                            }

                            synchronized (this) {
                                try {
                                    wait(10000);
                                } catch (InterruptedException e) {
                                    //dont do anything, this thread is not that important.
                                }
                            }
                        }
                    }
                }).start();


                _logger.info("Waiting for all threads to finish...");//_logger thread not included

                //wait for collectionThreads to finish...
                for (Thread tr : Thread.getAllStackTraces().keySet()) {
                    if (tr.getClass().equals(CollectionThread.class)) {
                        try {
                            tr.join();
                        } catch (InterruptedException e) {
                            //this shouldn't happen but if it does, the program keeps running.
                            _logger.error(tr.toString() + "was interrupted.");
                        }
                    }
                }

                _logger.info("All threads are finished.");
                _logger.info("Checked all links.");

                if (linksToBeChecked.count() == 0) {
                    _logger.info("Copying all links back to linksToBeChecked from linksChecked.");
                    cursor = linksChecked.find().iterator();

                    while (cursor.hasNext()) {

                        URLElement urlElement = new URLElement(cursor.next());
                        String url = urlElement.getUrl();
                        _logger.info("Adding " + url + " to linksToBeChecked.");

                        URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(url, urlElement.getCollection());
                        try {
                            linksToBeChecked.insertOne(urlElementToBeChecked.getMongoDocument());
                        } catch (MongoException e) {
                            //duplicate key error
                            //url is already in the database, do nothing
                        }


                    }
                } else {
                    _logger.info("There are new links in linksToBeChecked. Not copying links back from linksChecked. Will do it in the next run if linksToBeChecked is empty.");
                }


            } finally {
                cursor.close();
            }

            _logger.info("Done with the run. Running all of it again...");

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


    private static MongoDatabase getMongoDatabase() {
        _logger.info("Connecting to database...");

        MongoClient mongoClient;
        if (Configuration.DATABASE_URI.isEmpty()) {//if it is empty, try localhost
            mongoClient = MongoClients.create();
        } else {
            mongoClient = MongoClients.create(Configuration.DATABASE_URI);
        }

        MongoDatabase database = mongoClient.getDatabase(Configuration.DATABASE_NAME);

        _logger.info("Connected to database.");

        return database;

    }

}
