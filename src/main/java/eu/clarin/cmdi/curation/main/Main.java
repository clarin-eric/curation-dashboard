package eu.clarin.cmdi.curation.main;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public static void main(String[] args) {
	
	Main curator = new Main();
	
	for(String entity: args)
	    curator.curate(FileSystems.getDefault().getPath(entity));
    }

}
