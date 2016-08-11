package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.FileReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;

public class FileSizeValidator extends CMDSubprocessor {

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception{
		if (entity.getSize() > Configuration.MAX_FILE_SIZE) {
			addMessage(Severity.FATAL, "The file size exceeds the limit allowed (" + Configuration.MAX_FILE_SIZE + "B)");
			//don't assess when assessing collections
			if(Configuration.COLLECTION_MODE)
				throw new Exception("The file size exceeds the limit allowed (" + Configuration.MAX_FILE_SIZE + "B)");
		}

		report.fileReport = new FileReport();
		report.fileReport.size = entity.getSize();
		report.fileReport.location = entity.getPath().toString();
	}

	@Override
	public Score calculateScore(CMDInstanceReport report) {
		//in case that size exceeds the limit msgs will be created and it will contain a single msg
		return new Score(msgs == null? 1.0 : 0, 1.0, "file-size", msgs);
	}
}
