package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.InstanceFacetProcessor;
import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;
import eu.clarin.cmdi.curation.subprocessor.InstanceHeaderValidatior;
import eu.clarin.cmdi.curation.subprocessor.InstanceXMLValidator;
import eu.clarin.cmdi.curation.subprocessor.ProcessingStep;
import eu.clarin.cmdi.curation.subprocessor.URLValidator;

public class CMDInstanceProcessor extends AbstractProcessor<CMDInstanceReport> {

    @Override
    protected Collection<ProcessingStep> createPipeline() {
	return Arrays.asList(
		new FileSizeValidator(),
		new InstanceHeaderValidatior(),
		//new ResourceProxyValidator(),
		new InstanceXMLValidator(),
		new URLValidator(),
		new InstanceFacetProcessor());
    }

    @Override
    protected CMDInstanceReport createReport() {
	return new CMDInstanceReport();
    }

}
