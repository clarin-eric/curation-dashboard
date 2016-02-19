package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.main.Config;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.CMDIFaceting;
import eu.clarin.cmdi.curation.subprocessor.CMDIHeaderValidator;
import eu.clarin.cmdi.curation.subprocessor.CMDIXMLValidator;
import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;
import eu.clarin.cmdi.curation.subprocessor.HttpURLValidator;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import eu.clarin.cmdi.curation.subprocessor.ResourceProxyValidator;

public class CMDIProcessor extends AbstractProcessor<CMDIInstanceReport> {

    @Override
    protected Collection<ProcessingStep> createPipeline() {
	return Config.HTTP_VALIDATION ?
		Arrays.asList(
			new FileSizeValidator(),
			new CMDIHeaderValidator(),
			new ResourceProxyValidator(),
			new CMDIXMLValidator(),
			new HttpURLValidator(),
			new CMDIFaceting()) 
		: 
		Arrays.asList(
			new FileSizeValidator(),
			new CMDIHeaderValidator(),
			new ResourceProxyValidator(),
			new CMDIXMLValidator(),
			new CMDIFaceting());
    }

    @Override
    protected CMDIInstanceReport createReport() {
	return new CMDIInstanceReport();
    }

}
