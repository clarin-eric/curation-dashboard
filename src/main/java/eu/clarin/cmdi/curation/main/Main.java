package eu.clarin.cmdi.curation.main;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Main {

    public static void main(String[] args) throws Exception {

	CommandLineParser parser = new PosixParser();
	CommandLine cmd = parser.parse(createOptions(), args);

	// init configuration file
	if(cmd.hasOption('c'))
		Configuration.init(new File(cmd.getOptionValue('c')));
	else
		Configuration.initDefault();

	if (cmd.hasOption('p')) {
		Configuration.OUTPUT_DIRECTORY = null;
	}

	for (String input : cmd.getOptionValues('i')) {
	    Path path = FileSystems.getDefault().getPath(input);
	    new Curator().curate(path);
	}
    }
    
    private static Options createOptions() {
	Options options = new Options();
	Option config = new Option("c", "config", true, "path to configuration file");

	Option printOnScreen = new Option("p", "print-on-screen", false,
		"print the report on the screen instead of saving it in the file");
	printOnScreen.setRequired(false);

	Option input = new Option("i", "input", true, "input file or folder");
	input.setArgs(Option.UNLIMITED_VALUES);
	input.setRequired(true);

	options.addOption(config);
	options.addOption(printOnScreen);
	options.addOption(input);

	return options;
    }

}
