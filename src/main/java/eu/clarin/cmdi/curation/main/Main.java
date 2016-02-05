package eu.clarin.cmdi.curation.main;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDIProfile;
import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.entities.EntityTree;

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
		entity = new CMDIRecord(path, Files.size(path));
	    else if (path.toString().endsWith(".xsd"))
		entity = new CMDIProfile(path, Files.size(path));
	    else
		throw new IllegalArgumentException(
			"Curation module can process only xml and xsd files!\nPS\nAt least for now");

	    entity.genReport();
	    _logger.info(entity.toString());
	} catch (Exception e) {
	    _logger.error("Curation failed for " + path, e);
	}

	// Instant end = Instant.now();
	long end = System.currentTimeMillis();
	// Duration duration = Duration.between(start, end);
	// _logger.trace("Curation took " + duration.getSeconds() + " seconds");
	_logger.info("Curation lasted {}ms", end - start);
	;
    }

    public static void main(String[] args) {
	Path path1 = FileSystems.getDefault().getPath(
		"D:/data/cmdi/META_SHARE_3_0/08ae069e770a11e5a6e4005056b4002410c2850320274e04b9a6c6692883a054.xml");
	Path path2 = FileSystems.getDefault().getPath("D:/data/cmdi/Deutsches_Textarchiv/dta_386.xml");
	Path cmdi = FileSystems.getDefault().getPath("D:/data/cmdi");

	Path mee_240K = FileSystems.getDefault().getPath("D:/data/cmdi/Meertens_Institute_Metadata_Repository");
	Path bas_23K = FileSystems.getDefault().getPath("D:/data/cmdi/BAS_Repository");
	Path ehu_18 = FileSystems.getDefault().getPath("D:/data/cmdi/Euskal_Herriko_Unibertsitatea");
	Path unname_65K = FileSystems.getDefault().getPath("D:/data/cmdi/Unnamed_provider_at_dspace_library_uu_nl");

	Main curator = new Main();
	// curator.curate(path1); curator.curate(path2);
	curator.curate(ehu_18);
    }

}
