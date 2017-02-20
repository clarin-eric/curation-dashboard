package eu.clarin.web;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import eu.clarin.web.components.LinkButton;
import eu.clarin.web.views.Collections;
import eu.clarin.web.views.Footer;
import eu.clarin.web.views.Header;
import eu.clarin.web.views.Help;
import eu.clarin.web.views.Instances;
import eu.clarin.web.views.Profiles;
import eu.clarin.web.views.ResultView;

//@SuppressWarnings("serial")
@Theme("mytheme")
@Widgetset("eu.clarin.web.MyAppWidgetset")
@Title("Curation Module")
@JavaScript("vaadin://js/toggle.js")
public class MainUI extends UI {

	private VerticalLayout mainMenu, customMenu;
	private CssLayout viewArea;
	
	
	public void setCustomMenu(VerticalLayout customMenu){
		this.customMenu.addComponent(customMenu);
	}

	@Override
	protected void init(VaadinRequest request) {
					
		VerticalLayout main = new VerticalLayout();
		main.setSizeFull();

		Header header = new Header();
		Footer footer = new Footer();
		HorizontalLayout middle = createMiddle();

		main.addComponents(header, middle, footer);
		main.setExpandRatio(header, 2);
		main.setExpandRatio(middle, 17);
		main.setExpandRatio(footer, 1.5f);

		setNavigator(new Navigator(this, viewArea));
		addView("Instances", new Instances());
		addView("Profiles", new Profiles());
		addView("Collections", new Collections());
		//addView("SMC Browser", new SMC());
		addView("Help", new Help());
		
		getNavigator().addView("ResultView", new ResultView());
		
		//reset visibility for exportMenu
		//it is automatically set for coresponding views
		getNavigator().addViewChangeListener(new ViewChangeListener(){
			
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				customMenu.removeAllComponents();
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
		
		setContent(main);
		
		//if no params in url goto start page (CurationForm) else to params
		String view = request.getParameter("v-loc");
		view = view.replaceAll("%20", " ");
		getNavigator().navigateTo(view.contains("#!")? view.substring(view.indexOf("#!") + 2) : "Instances");

	}

	private void addView(final String viewName, View view) {
		getNavigator().addView(viewName, view);
		
		Button button = new LinkButton(viewName);
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(viewName);
			}
		});		
		
		mainMenu.addComponent(button);
	}
	
	private HorizontalLayout createMiddle() {
		HorizontalLayout middle = new HorizontalLayout();
		middle.setSizeFull();
		
		VerticalLayout menu = createMenu();
		
		viewArea = new CssLayout();
		viewArea.setSizeFull();		

		middle.addComponents(menu, viewArea);
		middle.setExpandRatio(menu, 15);
		middle.setExpandRatio(viewArea, 85);

		return middle;
	}
	
	private VerticalLayout createMenu(){
		VerticalLayout side = new VerticalLayout();
		side.setSizeFull();
		
		mainMenu = new VerticalLayout();
		mainMenu.setStyleName("form-top-margin-50");
		
		customMenu = new VerticalLayout();
		customMenu.addComponent(new Label("Hi there"));
		
		side.addComponents(mainMenu, customMenu);
		side.setExpandRatio(mainMenu, 6);
		side.setExpandRatio(customMenu, 4);
		
		return side;
	}
	
}
