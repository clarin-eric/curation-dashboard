package eu.clarin.web.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.declarative.Design;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.entities.CurationEntityType;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.web.Shared;
import eu.clarin.web.utils.XSLTTransformer;

@DesignRoot
public class ResultView extends Panel implements View{

	Label label;
	Button bXML;
	StreamResource xmlReport;
	

	XSLTTransformer transformer = new XSLTTransformer();

	public ResultView() {
		Design.read(this);
		
		xmlReport = new StreamResource(null, "");
		xmlReport.setMIMEType("application/xml");
		xmlReport.setCacheTime(0);
		
		BrowserWindowOpener popup = new BrowserWindowOpener(xmlReport);
		popup.setFeatures("");
		popup.extend(bXML);	

	}

	@Override
	public void enter(ViewChangeEvent event) {
		
		String type = event.getParameters().substring(0, event.getParameters().indexOf('/'));
		String value = event.getParameters().substring(type.length() + 1);

		switch (type) {
			case "instance":
				curate(CurationEntityType.INSTANCE, value);
				break;
			case "profile":
				curate(CurationEntityType.PROFILE, value);
				break;
			case "collection":
				curate(CurationEntityType.COLLECTION, value);
				break;
			}
	}


	private void curate(CurationEntityType type, String input) {
		Report r = null;
		try {
			switch (type) {
				case INSTANCE:
				case PROFILE:
					CurationModule curator = new CurationModule();
					// if profileId append CLARIN REST prefix
					URL url = new URL((!input.startsWith("http") ? CRService.REST_API : "") + input);
					r = type.compareTo(CurationEntityType.INSTANCE) == 0 ? curator.processCMDInstance(url)
							: curator.processCMDProfile(url);
					break;
				case COLLECTION:
					r = Shared.getCollectionReport(input);
					break;
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			r.toXML(out);
			label.setValue(transformer.transform(type, out.toString()));
			
			
			xmlReport.setStreamSource(new StreamSource() {
				@Override
				public InputStream getStream() {
					return new ByteArrayInputStream(out.toByteArray());
				}
			});			
			xmlReport.setFilename(r.getName());
			
			
		} catch (Exception e) {
			e.printStackTrace();

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String msg = "Error while curating " + type.toString().toLowerCase() + " from " + input + "!\n"
					+ errors.toString();
			label.setValue(msg);
		}
	}

}
