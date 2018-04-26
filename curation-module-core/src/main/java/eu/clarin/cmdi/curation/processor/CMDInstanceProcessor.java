package eu.clarin.cmdi.curation.processor;

import java.util.Arrays;
import java.util.Collection;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.subprocessor.*;

public class CMDInstanceProcessor extends AbstractProcessor<CMDInstanceReport> {

    @Override	
    protected Collection<ProcessingStep> createPipeline() {
		return (Configuration.COLLECTION_MODE)?
				Arrays.asList(
					new FileSizeValidator(),
					new CollectionInstanceHeaderProcessor(),
					new CollectionInstanceResourceProxyProcessor(),
					//new URLValidator(),//todo
//					new InstanceXMLValidator(),todo
					new InstanceXMLPopulatedValidator(),
					new CollectionInstanceFacetProcessor())
				:
				Arrays.asList(
					new FileSizeValidator(),
					new InstanceHeaderProcessor(),
					new InstanceResourceProxyProcessor(),
					new URLValidator(),
//					new InstanceXMLValidator(),//todo
					new InstanceXMLPopulatedValidator(),
					new InstanceFacetProcessor()
				);
				
			
    }

    @Override
    protected CMDInstanceReport createReport() {
    	return new CMDInstanceReport();
    }

}
