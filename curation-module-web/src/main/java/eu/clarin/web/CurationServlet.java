package eu.clarin.web;

import javax.servlet.ServletException;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;

public class CurationServlet extends VaadinServlet{
	private static final long serialVersionUID = -5238372988443088343L;
	
	private static final String PIWIK = "<!-- Piwik -->	"
			+ "<script type=\"text/javascript\">	 "
			+ "var _paq = _paq || [];"
			+ "	 _paq.push([\"setDomains\", [\"*.clarin.oeaw.ac.at/curate\"]]);"
			+ "	 _paq.push(['trackPageView']);"
			+ "	 _paq.push(['enableLinkTracking']);"
			+ "	 (function() {"
			+ "	   var u=\"//clarin.oeaw.ac.at/piwik/\";"
			+ "	   _paq.push(['setTrackerUrl', u+'piwik.php']);"
			+ "	   _paq.push(['setSiteId', 32]);"
			+ "	   var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];"
			+ "	   g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g,s);"
			+ "	 })();"
			+ "	</script>"
			+ "	<noscript><p><img src=\"//clarin.oeaw.ac.at/piwik/piwik.php?idsite=32\" style=\"border:0;\" alt=\"\" /></p></noscript>"
			+ "	<!-- End Piwik Code -->";
	
	@Override
	protected void servletInitialized() throws ServletException {
		super.servletInitialized();
		getService().addSessionInitListener(new SessionInitListener() {
			
			@Override
			public void sessionInit(SessionInitEvent event) throws ServiceException {
				event.getSession().addBootstrapListener(new BootstrapListener() {
					
					@Override
					public void modifyBootstrapPage(BootstrapPageResponse response) {
						response.getDocument().head().append(PIWIK);
					}
					
					@Override
					public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
						// TODO Auto-generated method stub
						
					}
				});
				
			}
		});
	}

}
