package eu.clarin.cmdi.curation.main;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.entities.CMDIProfile;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.entities.EntityTree;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.utils.TimeUtils;

public class Main {

    private static final Logger _logger = LoggerFactory.getLogger(Main.class);

    public void curate(Path path) {

	// Instant start = Instant.now();
	long start = System.currentTimeMillis();
	CurationEntity entity;
	try {
	    _logger.trace("curating " + path);
	    if (!Files.exists(path))
		throw new Exception(path + " doesn't exist");

	    if (Files.isDirectory(path)) {// check if path is a directory
		EntityTree entityTree = new EntityTree();
		Files.walkFileTree(path, entityTree);
		entity = entityTree.getRoot();
	    } else if (path.toString().endsWith(".xml"))
		entity = new CMDIInstance(path, Files.size(path));
	    else if (path.toString().endsWith(".xsd"))
		entity = new CMDIProfile(path, Files.size(path));
	    else
		throw new IllegalArgumentException(
			"Curation module can process only xml and xsd files!\nPS\nAt least for now");

	    Report report = entity.generateReport();
	    long end = System.currentTimeMillis();
	    _logger.info("Curation lasted {}", TimeUtils.humanizeTime(end - start));

	    File f = new File("D:/reports/" + path.getFileName());
	    report.marshal(System.out); // new FileOutputStream(f)

	} catch (Exception e) {
	    long end = System.currentTimeMillis();
	    _logger.error("Curation failed for " + path, e);
	    _logger.info("Curation lasted {}", TimeUtils.humanizeTime(end - start));
	}

    }
    
    private static Options createOptions(){
	Options options = new Options();
	Option config = new Option("c", "config", true, "path to configuration file");
	
	Option printOnScreen = new Option("ps", "print-on-screen", false, "print the report on the screen instead of saving it in the file");
	printOnScreen.setRequired(false);
	
	Option input = new Option("i", "input", true, "input file or folder");
	input.setRequired(true);
	
	options.addOption(config);
	options.addOption(printOnScreen);
	options.addOption(input);
	
	return options;
    }

    public static void main(String[] args) throws ParseException {
	
	//CommandLineParser parser = new PosixParser();
	//CommandLine cmd = parser.parse(createOptions(), args);
	
	
		
	Path path1 = FileSystems.getDefault().getPath(
		"D:/data/cmdi/META_SHARE_3_0/08ae069e770a11e5a6e4005056b4002410c2850320274e04b9a6c6692883a054.xml");
	Path path2 = FileSystems.getDefault().getPath("D:/data/cmdi/Deutsches_Textarchiv/dta_386.xml");
	Path cmdi = FileSystems.getDefault().getPath("D:/data/cmdi");
	Path test = FileSystems.getDefault().getPath("D:/data/test");

	Path ehu_18 = FileSystems.getDefault().getPath("D:/data/cmdi/Euskal_Herriko_Unibertsitatea");
	Path lbof_295 = FileSystems.getDefault().getPath("D:/data/cmdi/Language_Bank_of_Finland");
	Path eloftw_7K = FileSystems.getDefault().getPath("D:/data/cmdi/Ethnologue_Languages_of_the_World");
	Path bas_23K = FileSystems.getDefault().getPath("D:/data/cmdi/BAS_Repository/oai_BAS_repo_Corpora_aGender_101104.xml");
	Path unname_65K = FileSystems.getDefault().getPath("D:/data/cmdi/Unnamed_provider_at_dspace_library_uu_nl");
	Path mee_240K = FileSystems.getDefault().getPath("D:/data/cmdi/Meertens_Institute_Metadata_Repository");

	Main curator = new Main();
	// curator.curate(path1); curator.curate(path2);
	curator.curate(eloftw_7K);
    }

}
