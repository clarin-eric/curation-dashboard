package eu.clarin.cmdi.curation.main;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import eu.clarin.cmdi.curation.report.Report;

public class Main {

	public static void main(String[] args) throws Exception {

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(createOptions(), args);
		
		List<Report> reports = new LinkedList<>();

		// init configuration file
		if (cmd.hasOption("conf"))
			Configuration.init(new File(cmd.getOptionValue("conf")));
		else
			Configuration.initDefault();

		if (cmd.hasOption('p')) {
			Configuration.OUTPUT_DIRECTORY = null;
		}
		
		CurationModule curator = new CurationModule();
		
		if (cmd.hasOption("p")){//profile
			if (cmd.hasOption("id")){
				for(String id: cmd.getOptionValues("id"))
					reports.add(curator.processCMDProfile(id));
			}else if(cmd.hasOption("url")){
				for(String url: cmd.getOptionValues("url"))
					reports.add(curator.processCMDProfile(new URL(url)));
			}else
				throw new Exception("Only id and url options are allowed for profiles curation");
		}else if (cmd.hasOption("i")){//instance
			if(cmd.hasOption("url")){
				for(String url: cmd.getOptionValues("url"))
					reports.add(curator.processCMDInstance(new URL(url)));
			}else if(cmd.hasOption("path")){
				for(String path: cmd.getOptionValues("path"))
					reports.add(curator.processCMDInstance(Paths.get(path)));
			}else
				throw new Exception("Only path and url options are allowed for instances curation");
			
		}else if (cmd.hasOption("c")){//collection
			if(cmd.hasOption("path")){
				for(String path: cmd.getOptionValues("path"))
					reports.add(curator.processCollection(Paths.get(path)));
			}else
				throw new Exception("Only path is allowed for collection curation");
		}else
			throw new Exception("Curation module can curate profiles (-p), instances (-i) and collections (-c)");
		
		
		for(Report r: reports){
			r.toXML(System.out);
			System.out.println("-----------------------------------------------------------------");
			System.out.println();
			System.out.println();
		}
		
	}


	private static Options createOptions() {

		Option configurationFile = OptionBuilder
				.withArgName("file")
				.hasArg()
				.isRequired(false)
				.withDescription("a path to the configuration file")
				.create("config");

		Option profileCuration = OptionBuilder
				.withDescription("curate a profile")
				.create("p");

		Option instanceCuration = OptionBuilder
				.withDescription("curate an instance")
				.create("i");

		Option collectionCuration = OptionBuilder
				.withDescription("curate a collection")
				.create("c");

		OptionGroup curationGroup = new OptionGroup();
		curationGroup.addOption(profileCuration).addOption(instanceCuration).addOption(collectionCuration);
		curationGroup.setRequired(true);

		Option paramId = OptionBuilder
				.withArgName("profilesId")
				.hasArgs(Option.UNLIMITED_VALUES)
				.withDescription("Space separated CLARIN profile IDs in format: clarin.eu:cr1:p_xxx")
				.create("id");

		Option paramPath = OptionBuilder
				.withArgName("path")
				.hasArgs(Option.UNLIMITED_VALUES)
				.withDescription("Space separated paths to file or folder to be curated")
				.create("path");

		Option paramUrl = OptionBuilder
				.withArgName("url")
				.hasArgs(Option.UNLIMITED_VALUES)
				.withDescription("Space separated urls to profile or instane to be curated")
				.create("url");

		OptionGroup curationInputParams = new OptionGroup();
		curationInputParams.addOption(paramId).addOption(paramPath).addOption(paramUrl);
		curationInputParams.setRequired(true);

		Options options = new Options();
		options.addOption(configurationFile);
		options.addOptionGroup(curationGroup);
		options.addOptionGroup(curationInputParams);

		return options;
	}

}
