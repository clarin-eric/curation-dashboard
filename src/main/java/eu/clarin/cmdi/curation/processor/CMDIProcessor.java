package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.subprocessor.CMDIFaceting;
import eu.clarin.cmdi.curation.subprocessor.CMDIHeaderValidator;
import eu.clarin.cmdi.curation.subprocessor.CMDIXMLValidator;
import eu.clarin.cmdi.curation.subprocessor.ProcessingActivity;
import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;
import eu.clarin.cmdi.curation.subprocessor.HttpURLValidator;
import eu.clarin.cmdi.curation.subprocessor.ResourceProxyValidator;

public class CMDIProcessor extends AbstractProcessor{

	
	@Override
	protected Collection<ProcessingActivity> createPipeline() {
		return Arrays.asList(
				new FileSizeValidator(),
				new CMDIHeaderValidator(),
				new ResourceProxyValidator(),
				new CMDIXMLValidator(),
				new HttpURLValidator(),
				new CMDIFaceting()
			);
	}
}
