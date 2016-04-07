package eu.clarin.web;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import eu.clarin.web.components.InputForm;
import eu.clarin.web.components.SourceSelector;

/**
 * @author dostojic
 *
 */
@Theme("mytheme")
@Widgetset("eu.clarin.web.MyAppWidgetset")
public class CurationUI extends UI {

	public enum TYPE {
		PROFILE, INSTANCE
	}

	ObjectProperty reportOutput = new ObjectProperty<>("", String.class);

	@Override
	protected void init(VaadinRequest request) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		
		Layout header = createHeader();
		Layout content = createContent();
				
		layout.addComponents(header, content);
		
		layout.setExpandRatio(header, 1);
		layout.setExpandRatio(content, 9);
		
		layout.setSpacing(true);
		layout.setMargin(true);
		
		
		setContent(layout);		
		
		getPage().setTitle("Curation Module");

	}
	
	private Layout createHeader(){
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidth("100%");
		layout.setHeightUndefined();
		
		Label title = new Label("Curation Module");
		title.setStyleName("header-title");
				
		
		HeaderLogo acdhLogo = new HeaderLogo("img/acdh-logo.png", "http://www.oeaw.ac.at/acdh/");
		HeaderLogo clarinLogo = new HeaderLogo("img/clarin-logo.png", "https://www.clarin.eu/");
		
		layout.addComponents(title, acdhLogo, clarinLogo);
		
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		layout.setExpandRatio(title, 7);
		layout.setExpandRatio(acdhLogo, 2);
		layout.setExpandRatio(clarinLogo, 1);
		
		
		layout.setSpacing(true);
		
		return layout;
		
	}
	
	private Layout createContent(){
		
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setStyleName("content");

		InputForm instanceValidator = new InputForm(TYPE.INSTANCE, reportOutput);
		InputForm profileValidator = new InputForm(TYPE.PROFILE, reportOutput);

		TextArea report = new TextArea(reportOutput);
		report.setSizeFull();
		report.setReadOnly(true);

		layout.addComponents(instanceValidator, profileValidator, report);
		layout.setExpandRatio(instanceValidator, 1);
		layout.setExpandRatio(profileValidator, 1);
		layout.setExpandRatio(report, 8);

		layout.setMargin(true);
		layout.setSpacing(true);
		
		SourceSelector tabSheet = new SourceSelector(true, reportOutput);
		
		HorizontalLayout main = new HorizontalLayout();
		main.setSizeFull();
		main.addComponents(tabSheet, layout);
		
		main.setExpandRatio(tabSheet, 2);
		main.setExpandRatio(layout, 8);
		
		main.setMargin(true);
		
		return main;
	}
		
	
	class HeaderLogo extends Link{
		
		public HeaderLogo(String image, String url) {
			super(null, new ExternalResource(url));
			setIcon(new ThemeResource(image));
			setDescription(url);
			setStyleName("cm-logo");
			setHeightUndefined();
		}
		
	}
	
	
// MOVED TO web.xml
	// @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported =
	// true)
	// @VaadinServletConfiguration(ui = CurationUI.class, productionMode = true)
	// public static class MyUIServlet extends VaadinServlet {
	// }

}
