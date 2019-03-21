package eu.clarin.cmdi.curation.main;

import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.cr.PublicProfiles;
import eu.clarin.cmdi.curation.entities.CurationEntity.CurationEntityType;
import eu.clarin.cmdi.curation.report.CollectionsReport;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.utils.FileNameEncoder;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Main {

    public static void main(String[] args) throws Exception {

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

                /*
                 usage: curation module
                 -c                 curate a collection
                 -config <file>     a path to the configuration file
                 -i                 curate an instance
                 -id <profilesId>   Space separated CLARIN profile IDs in format:
                                    clarin.eu:cr1:p_xxx
                 -p                 curate a profile
                 -path <path>       Space separated paths to file or folder to be curated
                 -url <url>         Space separated urls to profile or instance to be
                                    curated
                * */
            }
        } catch (org.apache.commons.cli.ParseException e) {
            //do nothing
        }


        cmd = parser.parse(options, args);

        // init configuration file
        if (cmd.hasOption("config"))
            Configuration.init(cmd.getOptionValue("config"));
        else
            Configuration.initDefault();

/*        if (cmd.hasOption('p')) {
            Configuration.OUTPUT_DIRECTORY = null;
        }*/

        Configuration.enableProfileLoadTimer = false;

        CurationModule curator = new CurationModule();
        CurationEntityType type = null;

        if (cmd.hasOption("p")) {// profile
            type = CurationEntityType.PROFILE;
            if (cmd.hasOption("id")) {
                Configuration.OUTPUT_DIRECTORY = null;
                for (String id : cmd.getOptionValues("id"))
                    dumpAsXML(curator.processCMDProfile(id), type);
            } 
            else if (cmd.hasOption("url")) {
                Configuration.OUTPUT_DIRECTORY = null;
                for (String url : cmd.getOptionValues("url"))
                    dumpAsXML(curator.processCMDProfile(new URL(url)), type);
            } 
            else {
                //throw new Exception("Only id and url options are allowed for profiles curation");
                for (ProfileHeader pHeader : PublicProfiles.createPublicProfiles())
                        dumpAsXML(curator.processCMDProfile(pHeader.getId()), type);

            }
        } 
        else if (cmd.hasOption("i")) {// instance
            type = CurationEntityType.INSTANCE;
            if (cmd.hasOption("url")) {
                for (String url : cmd.getOptionValues("url"))
                    dumpAsXML(curator.processCMDInstance(new URL(url)), type);
            } 
            else if (cmd.hasOption("path")) {
                for (String path : cmd.getOptionValues("path"))
                    dumpAsXML(curator.processCMDInstance(Paths.get(path)), type);
            } 
            else
                throw new Exception("Only path and url options are allowed for instances curation");

        } 
        else if (cmd.hasOption("c")) {// collection
            type = CurationEntityType.COLLECTION;
            Configuration.COLLECTION_MODE = true;
            if (cmd.hasOption("path")) {
                for (String path : cmd.getOptionValues("path")) {
                    //dump(curator.processCollection(Paths.get(path)), type);
                    dumpAsXML(curator.processCollection(Paths.get(path)), type);
                    dumpAsHTML(curator.processCollection(Paths.get(path)), type);
                }
            } else
                throw new Exception("Only path is allowed for collection curation");
        } 
        else if (cmd.hasOption("r")) {// collection
            type = CurationEntityType.COLLECTION;
            Configuration.COLLECTION_MODE = true;
            
            Report report;

            if (cmd.hasOption("path")) {
                CollectionsReport overview = new CollectionsReport();
                
                
                    //dump(curator.processCollection(Paths.get(path)), type);
                    for(File file : new File(cmd.getOptionValues("path")[0],"cmdi").listFiles()) {
                        report = curator.processCollection(file.toPath());
                        dumpAsXML(report, type);
                        dumpAsHTML(report, type);
                        
                        overview.addReport(report);
                    }    

                dumpAsXML(overview, type);
            } 
            else
                throw new Exception("Only path is allowed for results curation");
        } 
        else
            throw new Exception("Curation module can curate profiles (-p), instances (-i), collections (-c) and results (-r)");
    }


    /*
     * private static void dump(Report report, CurationEntityType type) throws
     * Exception {
     * 
     * if (Configuration.SAVE_REPORT && Configuration.OUTPUT_DIRECTORY != null) {
     * Path path = null; switch (type) { case PROFILE: path =
     * Configuration.OUTPUT_DIRECTORY.resolve("profiles"); break; case INSTANCE:
     * path = Configuration.OUTPUT_DIRECTORY.resolve("instances"); break; case
     * COLLECTION: path = Configuration.OUTPUT_DIRECTORY.resolve("collections");
     * break; }
     * 
     * Files.createDirectories(path); String filename =
     * FileNameEncoder.encode(report.getName()) + ".xml"; path =
     * path.resolve(filename); report.toXML(Files.newOutputStream(path)); } else
     * {//print to console report.toXML(System.out); System.out.println(
     * "-----------------------------------------------------------------");
     * 
     * } }
     */
    
    private static void dumpAsXML(Object object, CurationEntityType type) throws Exception{
        JAXBContext jc = JAXBContext.newInstance(object.getClass());
        
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
        
        if (Configuration.SAVE_REPORT && Configuration.OUTPUT_DIRECTORY != null) {
            Path path = null;
            switch (type) {
                case PROFILE:
                    path = Configuration.OUTPUT_DIRECTORY.resolve("profiles");
                    break;
                case INSTANCE:
                    path = Configuration.OUTPUT_DIRECTORY.resolve("instances");
                    break;
                case COLLECTION:
                    path = Configuration.OUTPUT_DIRECTORY.resolve("collections");
                    break;
            }

            Files.createDirectories(path);
            String filename = FileNameEncoder.encode(object instanceof Report? ((Report)object).getName():object.getClass().getSimpleName()) + ".xml";
            path = path.resolve(filename);
            
            marshaller.marshal(object, Files.newOutputStream(path));
            
        } else {//print to console
            marshaller.marshal(object, System.out);

            System.out.println("-----------------------------------------------------------------");

        }
        
        
    }
    
    private static void dumpAsHTML(Object object, CurationEntityType type) throws TransformerException, JAXBException, IOException {
        Path path = Configuration.OUTPUT_DIRECTORY.resolve("html");

        Files.createDirectories(path);
        String filename = FileNameEncoder.encode(object instanceof Report? ((Report)object).getName():object.getClass().getName()) + ".html";
        path = path.resolve(filename);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        System.out.println("/xslt/" + object.getClass().getSimpleName() + "2HTML.xsl");
        Source xslt = new StreamSource(Main.class.getResourceAsStream("/xslt/" + object.getClass().getSimpleName() + "2HTML.xsl"));

        Transformer transformer = factory.newTransformer(xslt);
        transformer.transform(new JAXBSource(JAXBContext.newInstance(object.getClass()), object), new StreamResult(path.toFile()));                
        
    }
    
    private static void dumpAsTSV(Object object, CurationEntityType type) throws TransformerException, JAXBException, IOException {
        Path path = Configuration.OUTPUT_DIRECTORY.resolve("tsv");

        Files.createDirectories(path);
        String filename = FileNameEncoder.encode(object instanceof Report? ((Report)object).getName():object.getClass().getName()) + ".tsv";
        path = path.resolve(filename);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(Main.class.getResourceAsStream("/xslt/" + object.getClass().getSimpleName() + "2TSV.xsl"));

        Transformer transformer = factory.newTransformer(xslt);
        transformer.transform(new JAXBSource(JAXBContext.newInstance(object.getClass()), object), new StreamResult(path.toFile()));        
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
        
        Option resultsCuration = OptionBuilder.withDescription("curate a results folder of collections").create("r");

        OptionGroup curationGroup = new OptionGroup();
        curationGroup.addOption(profileCuration).addOption(instanceCuration).addOption(collectionCuration).addOption(resultsCuration);
        curationGroup.setRequired(true);

        Option paramId = OptionBuilder.withArgName("profilesId").hasArgs(Option.UNLIMITED_VALUES)
                .withDescription("Space separated CLARIN profile IDs in format: clarin.eu:cr1:p_xxx").create("id");

        Option paramPath = OptionBuilder.withArgName("path").hasArgs(Option.UNLIMITED_VALUES)
                .withDescription("Space separated paths to file or folder to be curated").create("path");

        Option paramUrl = OptionBuilder.withArgName("url").hasArgs(Option.UNLIMITED_VALUES)
                .withDescription("Space separated urls to profile or instance to be curated").create("url");
        
        Option paramPublicProfiles = OptionBuilder.withArgName("publicProfiles").hasArg(false)
                .withDescription("Creates reports for public profiles with status production").create("public");

        OptionGroup curationInputParams = new OptionGroup();
        curationInputParams.addOption(paramId).addOption(paramPath).addOption(paramUrl).addOption(paramPublicProfiles);
        curationInputParams.setRequired(true);

        Options options = new Options();
        options.addOption(configurationFile);
        options.addOptionGroup(curationGroup);
        options.addOptionGroup(curationInputParams);

        return options;
    }

    private static OutputStream getOutputStream(Report report, CurationEntityType type) throws IOException {
        if (Configuration.SAVE_REPORT && Configuration.OUTPUT_DIRECTORY != null) {
            Path path = null;
            switch (type) {
                case PROFILE:
                    path = Configuration.OUTPUT_DIRECTORY.resolve("profiles");
                    break;
                case INSTANCE:
                    path = Configuration.OUTPUT_DIRECTORY.resolve("instances");
                    break;
                case COLLECTION:
                    path = Configuration.OUTPUT_DIRECTORY.resolve("collections");
                    break;
            }

            Files.createDirectories(path);
            path = path.resolve(report.getName() + ".xml");
            return Files.newOutputStream(path);
        }

        return System.out;

    }
}
