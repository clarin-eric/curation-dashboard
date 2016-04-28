package eu.clarin.cmdi.curation.test.ccr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.Report;

public class CollectionAssement {
	
	public static void main(String[] args) throws Exception {

		Configuration.initDefault();
		Configuration.OUTPUT_DIRECTORY = Paths.get("D:/reports");
		Configuration.HTTP_VALIDATION = false;
		
		CurationModule module = new CurationModule();
		
		//r.toXML(new FileOutputStream(new File("D:/data/reports/" + collection + ".xml")));		
		
		//fileList("C:/harvester/cmdi/").forEach(System.out::println);
		
		for(Path collection: fileList("C:/harvester/cmdi/")){
			Report r = module.processCollection(collection);
			r.toXML(new FileOutputStream(new File("D:/reports/collections1/" + collection.getFileName() + ".xml")));
		}
	}
	
	public static List<Path> fileList(String directory) {
        List<Path> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
            	if(!path.getFileName().toString().equals("CLARIN_OAIProvider_Institute_of_Computer_Science_NLP_Group_University_of_Leipzig"))
            		fileNames.add(path);
            }
        } catch (IOException ex) {}
        return fileNames;
    }

}
