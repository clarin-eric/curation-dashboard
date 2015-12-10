package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.subprocessor.CMDIHeaderValidator;
import eu.clarin.cmdi.curation.subprocessor.CMDIValidator;
import eu.clarin.cmdi.curation.subprocessor.CurationStep;
import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;

public class CMDIProcessor extends AbstractProcessor{

	
	@Override
	protected Collection<CurationStep> initPipeline() {
		return Arrays.asList(
				new FileSizeValidator(),
				new CMDIHeaderValidator(),
				new CMDIValidator()
				);
	}
}
