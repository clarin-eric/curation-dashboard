//package eu.clarin.cmdi.curation.processor;
//
//import java.util.Collection;
//import java.util.LinkedList;
//
//import eu.clarin.cmdi.curation.subprocessor.CMDIXMLValidator;
//import eu.clarin.cmdi.curation.subprocessor.ProcessingActivity;
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
//		Collection<ProcessingActivity> pipeline = new LinkedList<ProcessingActivity>();
//
//		//add your validators/normalisators/enrichers/etc here 
//		pipeline.add(new FileSizeValidator());
//		pipeline.add(new CMDIXMLValidator());
//		
//		return new CMDIProcessor();
//	}
//	
//
//}
