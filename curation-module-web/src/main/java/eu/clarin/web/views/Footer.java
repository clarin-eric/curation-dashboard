package eu.clarin.web.views;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.Panel;
import com.vaadin.ui.declarative.Design;


@DesignRoot
public class Footer extends Panel{

	public Footer(){
		Design.read(this);
	}
}
