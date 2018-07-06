import com.mongodb.MongoException;
import com.mongodb.client.*;
import helpers.Configuration;
import httpLinkChecker.CollectionThread;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import urlElements.URLElement;
import urlElements.URLElementToBeChecked;

public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        if (args.length == 0) {
            logger.info("Usage: Please provide the config file path as a parameter.");
            System.exit(1);
        }

        Configuration.loadConfigVariables(args[0]);

        //TODO curation module populates with wrong collection names... todo in core module
        
        //todo make sure url is unique(done in database)(document it somewhere)
        //db.foo.createIndex({name:1}, {unique:true});

        //connect to mongod and get database
        MongoDatabase database = getMongoDatabase();

        //get links from linksToBeChecked
        MongoCollection<Document> linksToBeChecked = database.getCollection("linksToBeChecked");

        //get linksChecked
        MongoCollection<Document> linksChecked = database.getCollection("linksChecked");

        while (true) {

            MongoCursor<Document> cursor = linksToBeChecked.find().iterator();
            try {
                while (cursor.hasNext()) {
                    URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(cursor.next());

                    String collection = urlElementToBeChecked.getCollection();
                    String url = urlElementToBeChecked.getUrl();
                    logger.info("URL to be checked: " + url + ", from collection: " + collection);


                    CollectionThread t = getCollectionThreadByName(collection);
                    if (t == null) {
                        t = new CollectionThread(collection, linksToBeChecked, linksChecked);
                        t.urlQueue.add(url);
                        t.start();
                    } else {
                        t.urlQueue.add(url);
                    }


                    //todo OK THIS WORKS AS EXPECTED BUT JUST SEE IF IT WORKS CORRECTLY AGAIN...
                    //todo delete this, this is just for testing
                    logger.info("Say what?");
                    for (Thread tr : Thread.getAllStackTraces().keySet()) {
                        logger.info("###############################" + "Thread: " + tr.getName() + " is running." + "###############################");
                        if (tr.getClass().equals(CollectionThread.class)) {
                            logger.info("###############################" + "Collection thread: " + tr.getName() + " is running." + "###############################");
                            logger.info("###############################" + "It has " + ((CollectionThread) tr).urlQueue.size() + " in its queue." + "###############################");
                        }
                    }
                    //todo delete until here
                }


                logger.info("Checked all links.");

                logger.info("Copying all links back to linksToBeChecked from linksChecked.");
                cursor = linksChecked.find().iterator();

                while (cursor.hasNext()) {

                    URLElement urlElement = new URLElement(cursor.next());
                    String url = urlElement.getUrl();
                    logger.info("Adding " + url + " to linksToBeChecked.");

                    URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(url, urlElement.getCollection());
                    try {
                        linksToBeChecked.insertOne(urlElementToBeChecked.getMongoDocument());
                    } catch (MongoException e) {
                        //duplicate key error
                        //url is already in the database, do nothing
                    }


                }

            } finally {
                cursor.close();
            }

            logger.info("Done with the run. Running all of it again...");


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
        logger.info("Connecting to database...");
        MongoClient mongoClient = MongoClients.create();

        MongoDatabase database = mongoClient.getDatabase(Configuration.DATABASE_NAME);
        logger.info("Connected to database.");
        return database;

    }

}
