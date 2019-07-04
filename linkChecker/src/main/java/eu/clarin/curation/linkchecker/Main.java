package eu.clarin.curation.linkchecker;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import eu.clarin.curation.linkchecker.helpers.Configuration;
import eu.clarin.curation.linkchecker.threads.CollectionThread;
import eu.clarin.curation.linkchecker.threads.CollectionThreadManager;
import eu.clarin.curation.linkchecker.urlElements.URLElement;
import eu.clarin.curation.linkchecker.urlElements.URLElementToBeChecked;
import org.apache.commons.cli.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.clarin.curation.linkchecker.helpers.Configuration.DATABASE;


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
        CommandLine cmd = parser.parse(options, args);

        if (!cmd.hasOption("config")) {

            _logger.error("Usage: Please provide the config file path as a parameter.");
            System.exit(1);

        }

        Configuration.loadConfigVariables(cmd.getOptionValue("config"));

        CollectionThreadManager manager = new CollectionThreadManager();

        manager.start();

    }

}
