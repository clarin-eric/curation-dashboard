package eu.clarin.cmdi.curation.main;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import eu.clarin.cmdi.curation.entities.CurationEntityType;
import eu.clarin.cmdi.curation.report.Report;

public class Main {

	public static void main(String[] args) throws Exception{

		CommandLineParser parser = new PosixParser();

		Options helpOptions = createHelpOption();

		Options options = createOptions();

		CommandLine cmd = null;

		try {
			cmd = parser.parse(helpOptions, args);
			if (cmd.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("curation module", options);
				return;
			}
		} catch (org.apache.commons.cli.ParseException e) {
			// do nothing
		}

		

		cmd = parser.parse(options, args);

		List<Report> reports = new ArrayList<>();

		// init configuration file
		if (cmd.hasOption("conf"))
			Configuration.init(new File(cmd.getOptionValue("conf")));
		else
			Configuration.initDefault();

		if (cmd.hasOption('p')) {
			Configuration.OUTPUT_DIRECTORY = null;
		}

		CurationModule curator = new CurationModule();
		CurationEntityType type = null;

		if (cmd.hasOption("p")) {// profile
			type = CurationEntityType.PROFILE;
			if (cmd.hasOption("id")) {
				for (String id : cmd.getOptionValues("id"))
					reports.add(curator.processCMDProfile(id));
			} else if (cmd.hasOption("url")) {
				for (String url : cmd.getOptionValues("url"))
					reports.add(curator.processCMDProfile(new URL(url)));
			} else
				throw new Exception("Only id and url options are allowed for profiles curation");
		} else if (cmd.hasOption("i")) {// instance
			type = CurationEntityType.INSTANCE;
			if (cmd.hasOption("url")) {
				for (String url : cmd.getOptionValues("url"))
					reports.add(curator.processCMDInstance(new URL(url)));
			} else if (cmd.hasOption("path")) {
				for (String path : cmd.getOptionValues("path"))
					reports.add(curator.processCMDInstance(Paths.get(path)));
			} else
				throw new Exception("Only path and url options are allowed for instances curation");

		} else if (cmd.hasOption("c")) {// collection
			type = CurationEntityType.COLLECTION;
			if (cmd.hasOption("path")) {
				for (String path : cmd.getOptionValues("path"))
					reports.add(curator.processCollection(Paths.get(path)));
			} else
				throw new Exception("Only path is allowed for collection curation");
		} else
			throw new Exception("Curation module can curate profiles (-p), instances (-i) and collections (-c)");

		for (Report r : reports) {
			r.toXML(getOutputStream(r, type));
			System.out.println("-----------------------------------------------------------------");
			System.out.println();
			System.out.println();
		}

	}

	private static Options createHelpOption() {
		Option help = new Option("help", "print this message");
		Options options = new Options();
		options.addOption(help);
		return options;
	}

	private static Options createOptions() {

		Option configurationFile = OptionBuilder.withArgName("file").hasArg().isRequired(false)
				.withDescription("a path to the configuration file").create("config");

		Option profileCuration = OptionBuilder.withDescription("curate a profile").create("p");

		Option instanceCuration = OptionBuilder.withDescription("curate an instance").create("i");

		Option collectionCuration = OptionBuilder.withDescription("curate a collection").create("c");

		OptionGroup curationGroup = new OptionGroup();
		curationGroup.addOption(profileCuration).addOption(instanceCuration).addOption(collectionCuration);
		curationGroup.setRequired(true);

		Option paramId = OptionBuilder.withArgName("profilesId").hasArgs(Option.UNLIMITED_VALUES)
				.withDescription("Space separated CLARIN profile IDs in format: clarin.eu:cr1:p_xxx").create("id");

		Option paramPath = OptionBuilder.withArgName("path").hasArgs(Option.UNLIMITED_VALUES)
				.withDescription("Space separated paths to file or folder to be curated").create("path");

		Option paramUrl = OptionBuilder.withArgName("url").hasArgs(Option.UNLIMITED_VALUES)
				.withDescription("Space separated urls to profile or instane to be curated").create("url");

		OptionGroup curationInputParams = new OptionGroup();
		curationInputParams.addOption(paramId).addOption(paramPath).addOption(paramUrl);
		curationInputParams.setRequired(true);

		Options options = new Options();
		options.addOption(configurationFile);
		options.addOptionGroup(curationGroup);
		options.addOptionGroup(curationInputParams);

		return options;
	}
	
	private static OutputStream getOutputStream(Report report, CurationEntityType type) throws IOException{
		if(Configuration.SAVE_REPORT && Configuration.OUTPUT_DIRECTORY != null && !Configuration.OUTPUT_DIRECTORY.isAbsolute()){
			Path path = null;
			switch (type) {
			case PROFILE:
				path = Configuration.OUTPUT_DIRECTORY.resolve("profiles");
				break;
			case INSTANCE:
				path = Configuration.OUTPUT_DIRECTORY.resolve("profiles");
				break;
			case COLLECTION:
				path = Configuration.OUTPUT_DIRECTORY.resolve("profiles");
				break;
			}
			path = path.resolve(report.getName() + ".xml");
			return Files.newOutputStream(path);
		}
		
		return System.out;
		
	}
}
