package eu.clarin.cmdi.curation.subprocessor;

import eu.clarin.cmdi.curation.entities.CurationEntity;
import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Severity;

public class FileSizeValidator extends CurationStep{

	@Override
	public Report process(CurationEntity entity) {
		Report report = new Report("FileSizeReport");
		report.addMessage(new Message("Size: " + entity.getSize()));
		if(entity.getSize() > Config.MAX_SIZE_OF_FILE)
			report.addMessage(new Message(Severity.FATAL, "The file size exceeds the limit allowed (" + Config.MAX_SIZE_OF_FILE + "B)"));

		return report;
	}

}
