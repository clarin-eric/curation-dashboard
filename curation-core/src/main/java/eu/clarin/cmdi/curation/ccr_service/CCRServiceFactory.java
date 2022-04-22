package eu.clarin.cmdi.curation.ccr_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class CCRServiceFactory {
	private static Logger LOG = LoggerFactory.getLogger(CCRServiceFactory.class);
	static String CCR_REST_API_URL = "https://openskos.meertens.knaw.nl/ccr/api/";
	
	private static CCRServiceFactory fac = null;
	
	private CCRService crrService;

	private CCRServiceFactory(boolean enableTimer){//refresh concept cache
		try {
			crrService = new CCRService();

			//enableTimer is only set when ccrservice is called from the webmodule and not the coremodule
			//because coremodule doesnt terminate when there is a timer
			if(enableTimer){

				Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						synchronized (crrService) {

							crrService = new CCRService();

						}

					}
				}, 60 * 60 * 1000 /* Once per day*/);

			}


		} catch (Exception e) {
			LOG.error("", e);
		}
	}
	
	
	public static synchronized ICCRService getCCRService(boolean enableTimer){
		if(CCRServiceFactory.fac == null){
			CCRServiceFactory.fac = new CCRServiceFactory(enableTimer);
		}
		return CCRServiceFactory.fac.crrService;
	}
	
	public static void set_CCR_REST_API_URL(String newUrl){
		CCR_REST_API_URL = newUrl;
	}
}
