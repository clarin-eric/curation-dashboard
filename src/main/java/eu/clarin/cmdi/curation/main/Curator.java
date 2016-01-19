package eu.clarin.cmdi.curation.main;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.entities.CurationEntity.RecordStatPrintLvl;
import eu.clarin.cmdi.curation.entities.EntityTree;

public class Curator {
	
	private static final Logger _logger = LoggerFactory.getLogger(Curator.class);
	
	
	public void curate(Path path){
		CurationEntity.RECORD_STAT_PRINT_LVL = RecordStatPrintLvl.ALL;
		
		//Instant start = Instant.now();
		long start = System.currentTimeMillis();
		CurationEntity entity;
		try {			
			_logger.trace("curating " + path);
			if(!Files.exists(path))
				throw new Exception(path + " doesn't exist");
			
			if(Files.isDirectory(path)){//check if path is a directory
				EntityTree entityTree = new EntityTree();
				Files.walkFileTree(path, entityTree);
				entity = entityTree.getRoot();
			}
			else
				if(path.toString().endsWith(".xml"))
					entity = new CMDIRecord(path, Files.size(path));
				else if(path.toString().endsWith(".xsd"))
					throw new UnsupportedOperationException();	
				else
					throw new IllegalArgumentException("Curation module can process only xml and xsd files!\nPS\nAt least for now");			
			
			entity.genReport();
			_logger.info(entity.toString());
		} catch (Exception e) {
			_logger.error("Curation failed for " + path, e);
		}
		
		//Instant end = Instant.now();
		long end = System.currentTimeMillis();
		//Duration duration = Duration.between(start, end);
		//_logger.trace("Curation took " + duration.getSeconds() + " seconds");
		_logger.info("Curation lasted {}ms", end - start);;
	}
	

	public static void main(String[] args){
		Path path1 = FileSystems.getDefault().getPath("D:/data/cmdi/The_Tibetan_and_Himalayan_Library/oai_corpus_11022000000007F6E8.xml");
		Path path2 = FileSystems.getDefault().getPath("D:/data/cmdi/The_Tibetan_and_Himalayan_Library/oai_THDL_virginia_edu_All.xml");
		Path path3 = FileSystems.getDefault().getPath("D:/data/cmdi/Deutsches_Textarchiv/dta_386.xml");
		Path dir = 	 FileSystems.getDefault().getPath("D:/data/cmdi/__TEST");
		Curator curator = new Curator();
		//curator.curate(path1); curator.curate(path2);
		curator.curate(path3);
	}

}
