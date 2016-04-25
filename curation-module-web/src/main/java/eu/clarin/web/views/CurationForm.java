package eu.clarin.web.views;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;

import eu.clarin.web.utils.InputValidator;


@DesignRoot
@Title("Curation Module - Form")
public class CurationForm extends VerticalLayout implements View{
	
	TextField tfInstance, tfProfile;
	Button bInstance, bProfile;

	public CurationForm(){
		Design.read(this);
		
		tfInstance.addValidator(new InputValidator(true));
		tfProfile.addValidator(new InputValidator(false));
				
		bInstance.addClickListener(event -> curate(tfInstance, true));
		bProfile.addClickListener(event -> curate(tfProfile, false));		
	}

	@Override
	public void enter(ViewChangeEvent event) {
		tfInstance.setValidationVisible(false);
		tfProfile.setValidationVisible(false);
	}
	
	
	private void curate(TextField input, boolean isInstance){
		//validate input
		input.setValidationVisible(true);
		input.addValidator(new InputValidator(isInstance));
		input.validate();
		if(!input.isValid())
			return;
		
		// all processing is done in the ResultView class
		getUI().getNavigator().navigateTo("ResultView/" + (isInstance? "instance" : "profile") + "/" + input.getValue());
	}
}
