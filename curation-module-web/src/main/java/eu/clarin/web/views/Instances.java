package eu.clarin.web.views;


import org.apache.commons.validator.routines.UrlValidator;

import com.vaadin.annotations.Title;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.clarin.web.components.FileDropBox;

@Title("Curation Module - Form")
public class Instances extends VerticalLayout implements View{
	
	private final TextField input;
	private final Button assessButton;
	
	private final UrlValidator urlValidator;

	public Instances(){
		setSpacing(true);
		setMargin(true);
		
		input = new TextField();
		input.setInputPrompt("CMD Record URL");
		input.setWidth(400.0f, Unit.PIXELS);
		input.addValidator(new Validator(){
			@Override
			public void validate(Object value) throws InvalidValueException {				
				if(!urlValidator.isValid(value.toString()))
					throw new InvalidValueException("");
				}	
		});
		
		assessButton = new Button("Assess");
		assessButton.addClickListener(event -> assess());
		
		final Label infoLabel = new Label("Drag here a CMDI recordName");
		
		
		final HorizontalLayout dropPaneContainer = new HorizontalLayout();
		final Label dummy = new Label();
		dummy.setWidth("100%");
		
		final HorizontalLayout dropPane = new HorizontalLayout(infoLabel);
		dropPane.setComponentAlignment(infoLabel, Alignment.MIDDLE_CENTER);
        dropPane.setWidth(280.0f, Unit.PIXELS);
        dropPane.setHeight(200.0f, Unit.PIXELS);
        dropPane.addStyleName("drop-area");
        
        final ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        dropPane.addComponent(progressBar);
		
        final FileDropBox dropBox = new FileDropBox(dropPane, progressBar);        
        
        dropPaneContainer.addComponents(dropBox, dummy);
        
        addComponents(new Label(), input, new Label(), new Label(), dropPaneContainer, new Label(), new Label(), assessButton);
       //setComponentAlignment(dropBox, Alignment.MIDDLE_CENTER);
        
        urlValidator = new UrlValidator();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		resetComponents();
	}
	
	
	private void assess(){
		resetComponents();
		input.setValue(input.getValue().trim());
		
		try{
			input.validate();
			getUI().getNavigator().navigateTo("ResultView/instance/url/" + input.getValue());		
		}catch(InvalidValueException e){
			input.setValidationVisible(true);
			input.setComponentError(new UserError(""));
			input.setCaption("Entered URL is invalid");
			return;
		}				
	}
	
	private void resetComponents(){
		input.setValidationVisible(false);
		input.setComponentError(null);
		input.setCaption("");
	}
}
