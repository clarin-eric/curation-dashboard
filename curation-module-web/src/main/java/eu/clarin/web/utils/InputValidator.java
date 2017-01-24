package eu.clarin.web.utils;

import com.vaadin.data.Validator;

public class InputValidator implements Validator{
	
	static final String PROFILE_ID_FORMAT = "clarin\\.eu:cr1:p_[0-9]+";
	static final String URL_FORMAT = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	
	boolean isInstance;	
	
	public InputValidator(boolean isInstance) {
		this.isInstance = isInstance;
	}

	@Override
	public void validate(Object value) throws InvalidValueException {
		String val = (String) value;
		
		if(val == null || val.length() == 0) throw new InvalidValueException("Value is missing!");
				
		if(isInstance){
			if(!val.matches(URL_FORMAT))
				throw new InvalidValueException("Invalid value, URL expected!");
		}
		else{//profile: URL or profileID
			if(!val.matches(URL_FORMAT) && !val.matches(PROFILE_ID_FORMAT))
				throw new InvalidValueException("Invalid value, URL or Profile ID expected!");
		}
	}	

}
