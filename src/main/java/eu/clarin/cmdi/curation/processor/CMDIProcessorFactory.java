//package eu.clarin.cmdi.curation.processor;
//
//import java.util.Collection;
//import java.util.LinkedList;
//
//import eu.clarin.cmdi.curation.subprocessor.CMDIValidator;
//import eu.clarin.cmdi.curation.subprocessor.CurationTask;
//import eu.clarin.cmdi.curation.subprocessor.FileSizeValidator;
//
//
//public class CMDIProcessorFactory implements AbstractProcessorFactory {
//	
//	//Singleton
//	private final static CMDIProcessorFactory instance = new CMDIProcessorFactory();
//	
//	private CMDIProcessorFactory(){}
//	
//	public static CMDIProcessorFactory getInstance(){
//		return instance;
//	}
//
//	@Override
//	public synchronized CMDIProcessor createCMDIProcessor() {
//		Collection<CurationTask> pipeline = new LinkedList<CurationTask>();
//
//		//add your validators/normalisators/enrichers/etc here 
//		pipeline.add(new FileSizeValidator());
//		pipeline.add(new CMDIValidator());
//		
//		return new CMDIProcessor();
//	}
//	
//
//}
