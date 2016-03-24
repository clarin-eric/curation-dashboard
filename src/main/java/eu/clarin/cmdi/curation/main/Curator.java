/**
 * 
 */
package eu.clarin.cmdi.curation.main;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CMDProfile;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.entities.EntityTree;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.utils.TimeUtils;

/**
 * @author dostojic
 *
 */
public class Curator {

    private static final Logger _logger = LoggerFactory.getLogger(Curator.class);

    public void curate(String profileId) {
	long start = System.currentTimeMillis();

	CMDProfile profile = new CMDProfile(profileId);
	Report report = profile.generateReport();

	long end = System.currentTimeMillis();
	_logger.info("Curation lasted {}", TimeUtils.humanizeTime(end - start));
	
	try {
	    saveReport(report, profileId + ".xml");
	}  catch (Exception e) {	   
	    _logger.error("Curation failed for " + profileId, e);
	   
	}finally {
	    end = System.currentTimeMillis();
	    _logger.info("Curation lasted {}", TimeUtils.humanizeTime(end - start));
	}

    }

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
		entity = new CMDInstance(path, Files.size(path));
	    else if (path.toString().endsWith(".xsd"))
		entity = new CMDProfile(path, Files.size(path));
	    else
		throw new IllegalArgumentException(
			"Curation module can process only xml and xsd files!\nPS\nAt least for now");

	    Report report = entity.generateReport();
	    long end = System.currentTimeMillis();
	    _logger.info("Curation lasted {}", TimeUtils.humanizeTime(end - start));
	    
	    String output = Configuration.OUTPUT_DIRECTORY + "/" + path.getFileName();
	    if (Files.isDirectory(path))
		    output += ".xml";
	   
	    saveReport(report, output); 
	   

	} catch (Exception e) {	   
	    _logger.error("Curation failed for " + path, e);
	   
	}finally {
	    long end = System.currentTimeMillis();
	    _logger.info("Curation lasted {}", TimeUtils.humanizeTime(end - start));
	}

    }

    private void saveReport(Report report, String path) throws Exception {
	if (Configuration.OUTPUT_DIRECTORY == null) {
	    report.marshal(System.out);
	} else {
	    String output = Configuration.OUTPUT_DIRECTORY + "/" + path;
	    report.marshal(new FileOutputStream(output));
	}
    }

}
