package eu.clarin.web.components;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.main.CurationModule;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.web.CurationUI.TYPE;

public class InputForm extends HorizontalLayout {
	
	final TextField input = new TextField();
	final Button validate = new Button("Validate");
	
	

	public InputForm(final TYPE type, ObjectProperty reportOutput) {

		setSizeFull();

		if(type == TYPE.INSTANCE){
			input.setInputPrompt("URL of an instance");
		}else{
			input.setInputPrompt("URL or ID of a profile");
		}
		
		input.setWidth(90, Unit.PERCENTAGE);

		validate.addClickListener(event -> {
			Report r = null;
			try {
				CurationModule curator = new CurationModule();
				URL url = null;
				
				//profile id or an error
				if(input.getValue().startsWith("clarin.eu:cr1:p_")){
					url = new URL(CRService.REST_API + input.getValue());
				}else
					url = new URL(input.getValue());
				
				r = type == TYPE.INSTANCE ? curator.processCMDInstance(url) : url==null ? curator.processCMDProfile(input.getValue()) : curator.processCMDProfile(url);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				r.toXML(out);
				reportOutput.setValue(out.toString());

			} catch (Exception e) {
				e.printStackTrace();
				reportOutput.setValue("Error while curating " + (type == TYPE.INSTANCE ? "instance" : "profile")
						+ " from " + input.getValue() + "!\n" + e.toString());
			}
		});

		addComponents(input, validate);
	}
}
