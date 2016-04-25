package eu.clarin.web.views;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.declarative.Design;


@DesignRoot
@Title("Curation Module - Help")
public class Help extends VerticalLayout implements View{

	public Help(){
		Design.read(this);
	}

	@Override
	public void enter(ViewChangeEvent event) {}
}
