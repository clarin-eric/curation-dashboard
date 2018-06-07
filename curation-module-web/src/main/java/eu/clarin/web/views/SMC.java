package eu.clarin.web.views;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;

import eu.clarin.cmdi.curation.cr.CRService;

@Title("Curation Module - SMC")
@DesignRoot
public class SMC extends VerticalLayout implements View{
	
	final static String SMC_URL = "https://clarin.oeaw.ac.at/exist/apps/smc-browser/index.html?"
			+ "graph=smc-graph-groups-profiles-datcats-rr.js&depth-before=7&depth-after=2&"
			+ "link-distance=120&charge=250&friction=75&gravity=10&node-size=4&link-width=1&labels=show&curve=straight-arrow&layout=horizontal-tree&selected"
			+ "=clarin_eucr1PROFILEID&";
	
	
	BrowserFrame smc;
	
	public SMC(){
		Design.read(this);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		if(event.getParameters().isEmpty()){//click on menu
			smc.setSource(new ExternalResource("https://clarin.oeaw.ac.at/exist/apps/smc-browser/index.html"));
		}else{
			String id = event.getParameters().substring(CRService.PROFILE_PREFIX.length());
			smc.setSource(new ExternalResource(SMC_URL.replace("PROFILEID", id)));
		}
	}

}
