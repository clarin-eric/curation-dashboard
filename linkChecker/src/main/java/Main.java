import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.UpdateOptions;
import httpLinkChecker.HTTPLinkChecker;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import urlElements.URLElement;
import urlElements.URLElementToBeChecked;

import java.io.IOException;

import static com.mongodb.client.model.Filters.eq;


public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        //todo add test databases and collections and write tests for them

        //todo make sure url is unique(done in database)(document it somewhere)

        //connect to mongod and get database
        MongoDatabase database = getMongoDatabase();

        //get links from linksToBeChecked
        MongoCollection<Document> linksToBeChecked = database.getCollection("linksToBeChecked");

        //get linksChecked
        MongoCollection<Document> linksChecked = database.getCollection("linksChecked");

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
                    linksChecked.findOneAndReplace(filter, urlElement.getMongoDocument(),findOneAndReplaceOptions.upsert(true));

                } catch (IOException e) {
                    logger.error("There is an error with the URL: " + urlElementToBeChecked.getUrl() + " . It is not being checked.");

                }

                //delete from linksToBeChecked(whether successful or there was an error, ist wuascht)
                linksToBeChecked.deleteOne(eq("url", urlElementToBeChecked.getUrl()));

            }
        } finally {
            cursor.close();
        }

        //todo
        //when linksToBeChecked is empty, fill it by copying all links from linksChecked

        //todo make it all a never ending loop

    }


    private static MongoDatabase getMongoDatabase() {
        logger.info("Connecting to database...");
        MongoClient mongoClient = MongoClients.create();

        MongoDatabase database = mongoClient.getDatabase("links");
        logger.info("Connected to database.");
        return database;

    }

}
