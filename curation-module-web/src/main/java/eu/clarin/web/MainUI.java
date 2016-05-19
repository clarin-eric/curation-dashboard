package eu.clarin.web;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import eu.clarin.web.views.Collections;
import eu.clarin.web.views.CurationForm;
import eu.clarin.web.views.Footer;
import eu.clarin.web.views.Header;
import eu.clarin.web.views.Help;
import eu.clarin.web.views.PublicProfiles;
import eu.clarin.web.views.ResultView;
import eu.clarin.web.views.SMC;

//@SuppressWarnings("serial")
@Theme("mytheme")
@Widgetset("eu.clarin.web.MyAppWidgetset")
@Title("Curation Module")
public class MainUI extends UI {

	VerticalLayout menu;
	CssLayout viewArea;

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
		addView("Form", new CurationForm());
		addView("Public Profiles", new PublicProfiles());
		addView("Collections", new Collections());
		addView("SMC Browser", new SMC());
		addView("Help", new Help());
		
		//addView();
		getNavigator().addView("ResultView", new ResultView());
		

		setContent(main);
		getNavigator().navigateTo("Form");

	}

	private void addView(final String viewName, View view) {
		getNavigator().addView(viewName, view);
		
		Button button = new Button(viewName, new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(viewName);
			}
		});		
		button.setWidth("100%");
		menu.addComponent(button);
	}
	
	private HorizontalLayout createMiddle() {
		HorizontalLayout middle = new HorizontalLayout();
		middle.setSizeFull();

		menu = new VerticalLayout();
		menu.setMargin(true);

		viewArea = new CssLayout();
		viewArea.setSizeFull();

		middle.addComponents(menu, viewArea);
		middle.setExpandRatio(menu, 15);
		middle.setExpandRatio(viewArea, 85);

		return middle;

	}

}
