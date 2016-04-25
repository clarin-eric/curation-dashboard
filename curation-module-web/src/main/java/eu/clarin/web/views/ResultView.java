package eu.clarin.web.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
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
	String xml, name;

	XSLTTransformer transformer = new XSLTTransformer();

	public ResultView() {
		Design.read(this);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		
		String type = event.getParameters().substring(0, event.getParameters().indexOf('/'));
		String value = event.getParameters().substring(type.length() + 1);

		switch (type) {
		case "xml":
			openXML();
			break;
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

	private void openXML() {
		StreamResource xmlStream = new StreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(xml.getBytes());
			}
		}, name);
		xmlStream.setMIMEType("application/xml");
		xmlStream.setCacheTime(0);
		String wind = "_blank";
		Page.getCurrent().open(xmlStream, wind, false);

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
			name = r.getName();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			r.toXML(out);
			xml = out.toString();
			label.setValue(transformer.transform(type, xml));

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
