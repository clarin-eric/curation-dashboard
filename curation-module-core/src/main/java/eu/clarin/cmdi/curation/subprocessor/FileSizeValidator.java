package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.nio.file.Files;

import javax.xml.transform.TransformerException;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.instance_parser.InstanceParser;
import eu.clarin.cmdi.curation.io.FileSizeException;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.FileReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;

public class FileSizeValidator extends CMDSubprocessor {

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception{
		report.fileReport = new FileReport();
		report.fileReport.size = entity.getSize();
		if(entity.getUrl()!=null){
			report.fileReport.location = entity.getUrl().replaceAll("/", "-");
		}else{
			report.fileReport.location = entity.getPath().toString();
		}


		if (report.fileReport.size > Configuration.MAX_FILE_SIZE) {
			addMessage(Severity.FATAL, "The file size exceeds the limit allowed (" + Configuration.MAX_FILE_SIZE + "B)");
			//don't assess when assessing collections
			if(Configuration.COLLECTION_MODE)
				throw new FileSizeException(entity.getPath().getFileName().toString(), report.fileReport.size);
		}

		InstanceParser transformer = new InstanceParser();
		try {
			entity.setParsedInstance(transformer.parseIntance(Files.newInputStream(entity.getPath())));
		} catch (TransformerException | IOException e) {
			throw new Exception("Unable to parse CMDI instance " + entity.getPath().toString(), e);
		}


	}

	@Override
	public Score calculateScore(CMDInstanceReport report) {
		//in case that size exceeds the limit msgs will be created and it will contain a single msg
		return new Score(msgs == null? 1.0 : 0, 1.0, "file-size", msgs);
	}
}
