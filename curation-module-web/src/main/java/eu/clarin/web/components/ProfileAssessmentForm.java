package eu.clarin.web.components;

import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.server.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

import eu.clarin.web.utils.InputValidator;

public class ProfileAssessmentForm extends Window{
	
	private TextField input;
	private Button button;
	
	private static final long serialVersionUID = -3070782574620406486L;
	
	public ProfileAssessmentForm() {
		super("Profile Assesment Form");
		
		setWidth(400, Unit.PIXELS);
		setHeight(220.0f, Unit.PIXELS);
		setResizable(false);
		center();
		
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);
		content.setMargin(true);
		initUrlInput();
		initButton();
		
		content.addComponents(input, button);
		
		content.setComponentAlignment(input, Alignment.MIDDLE_LEFT);
		content.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);
		
		setContent(content);
	}
	
	private void initUrlInput(){
		input = new TextField();
		input.addValidator(new InputValidator(false));
		input.setWidth("100%");
		input.setValidationVisible(false);
		input.setCaption("");
		
		input.setInputPrompt("Insert profiles URL or ID");
	}

	private void initButton(){
		button = new Button("Assess");
		button.setWidth("15ex");
		button.addClickListener(event -> curate());
		
	}
	
	private void curate(){
		input.setValue(input.getValue().trim());
		resetComponents();		
		try{
			//validate input
			input.validate();
		}catch(InvalidValueException ex){			
			input.setValidationVisible(true);
			input.setComponentError(new UserError(""));
			input.setCaption(ex.getMessage());
			return;
		}
		
		if(!input.isValid())
			return;
		
		// all processing is done in the ResultView class
		boolean isURL = input.getValue().startsWith("http");
			
		getUI().getNavigator().navigateTo("ResultView/profile/"
				+ (isURL? "url" : "id") + "/"
				+ input.getValue());
		this.close();
	}
	
	private void resetComponents(){
		input.setValidationVisible(false);
		input.setComponentError(null);
		input.setCaption("");
	}
	
}
