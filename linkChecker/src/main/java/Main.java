import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import httpLinkChecker.HTTPLinkChecker;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import urlElements.URLElement;
import urlElements.URLElementToBeChecked;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;


public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private static Properties properties = new Properties();


    public static void main(String[] args) {

        if (args.length == 0) {
            logger.info("Usage: Please provide the config file path as a parameter.");
            System.exit(1);
        }
        String configPath = args[0];
        try {
            properties.load(new FileInputStream(configPath));
        } catch (IOException e) {
            logger.error("Can't load properties file: " + e.getMessage());
            System.exit(1);
        }


        //todo add test databases and collections and write tests for them

        //todo make sure url is unique(done in database)(document it somewhere)
        //db.foo.createIndex({name:1}, {unique:true});

        //connect to mongod and get database
        MongoDatabase database = getMongoDatabase();

        //get links from linksToBeChecked
        MongoCollection<Document> linksToBeChecked = database.getCollection(properties.getProperty("linksToBeCheckedCollectionName"));

        //get linksChecked
        MongoCollection<Document> linksChecked = database.getCollection(properties.getProperty("linksCheckedCollectionName"));

//        while (true) {

            //todo make this paralel with taking collections into account
            MongoCursor<Document> cursor = linksToBeChecked.find().iterator();
            try {
                while (cursor.hasNext()) {
                    URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(cursor.next());

                    logger.info("URL to be checked: " + urlElementToBeChecked.getUrl() + ", from collection: " + urlElementToBeChecked.getCollection());

                    HTTPLinkChecker httpLinkChecker = new HTTPLinkChecker();

                    try {

                        //todo stream according to collection or something
                        //check the link
                        URLElement urlElement = httpLinkChecker.checkLink(urlElementToBeChecked.getUrl(), 0, 0);
                        urlElement.setCollection(urlElementToBeChecked.getCollection());

                        //replace if the url is in linksChecked already
                        //if not add new
                        FindOneAndReplaceOptions findOneAndReplaceOptions = new FindOneAndReplaceOptions();
                        Bson filter = Filters.eq("url", urlElement.getUrl());
                        linksChecked.findOneAndReplace(filter, urlElement.getMongoDocument(), findOneAndReplaceOptions.upsert(true));

                    } catch (IOException e) {
                        logger.error("There is an error with the URL: " + urlElementToBeChecked.getUrl() + " . It is not being checked.");

                    }

                    //delete from linksToBeChecked(whether successful or there was an error, ist wuascht)
                    linksToBeChecked.deleteOne(eq("url", urlElementToBeChecked.getUrl()));

                }


                logger.info("Checked all links.");

                logger.info("Copying all links back to linksToBeChecked from linksChecked.");
                cursor = linksChecked.find().iterator();

                while (cursor.hasNext()) {

                    URLElement urlElement = new URLElement(cursor.next());
                    String url = urlElement.getUrl();
                    logger.info("Adding " + url + " to linksToBeChecked.");

                    URLElementToBeChecked urlElementToBeChecked = new URLElementToBeChecked(url, urlElement.getCollection());
                    linksToBeChecked.insertOne(urlElementToBeChecked.getMongoDocument());

                }

            } finally {
                cursor.close();
            }

            logger.info("Done with the run. Running all of it again...");


//        }

    }


    private static MongoDatabase getMongoDatabase() {
        logger.info("Connecting to database...");
        MongoClient mongoClient = MongoClients.create();

        MongoDatabase database = mongoClient.getDatabase(properties.getProperty("databaseName"));
        logger.info("Connected to database.");
        return database;

    }

}
