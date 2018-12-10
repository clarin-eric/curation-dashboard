package eu.clarin.web.components;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

public class LinkButton extends Button{

	public LinkButton(String caption){
		super(caption);
		this.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
		this.addStyleName("link");
	}
}
