package eu.clarin.cmdi.curation.xml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.cr.CRConstants;

public class CMDXPathService {

	private static final Logger _logger = LoggerFactory.getLogger(CMDXPathService.class);

	private VTDNav navigator = null;
	
	public CMDXPathService(VTDNav navigator){
		this.navigator = navigator;
	}

	public CMDXPathService(Path path) throws Exception {
		VTDGen parser = new VTDGen();
		try {
			parser.setDoc(Files.readAllBytes(path));
			parser.parse(true);
			navigator = parser.getNav();
			parser = null;
		} catch (IOException | ParseException e) {
			throw new Exception("Errors while parsing " + path, e);
		}
	}

	public CMDXPathService(String url) throws Exception {
		VTDGen parser = new VTDGen();

		if (!parser.parseHttpUrl(url, true))
			throw new Exception("Errors while parsing " + url);

		navigator = parser.getNav();
		parser = null;
	}

	public VTDNav getNavigator() {
		return navigator;
	}

	public String getXPathValue(String xpath) throws Exception {
		String result = null;
		try {
			navigator.toElement(VTDNav.ROOT);
			AutoPilot ap = new AutoPilot(navigator);
			ap.selectXPath(xpath);
			int index = ap.evalXPath();
			if (index != -1)
				result = navigator.toString(index).trim();
		} catch (Exception e) {
			throw new Exception("Errors while performing xpath operation:" + xpath, e);
		}
		return result;
	}
	
	public Collection<String> getXPathValues(String xpath) throws Exception {
		
		Collection<String> result = new LinkedList<>();
		try {
			navigator.toElement(VTDNav.ROOT);
			AutoPilot ap = new AutoPilot(navigator);
			ap.selectXPath(xpath);
			while(true){
				int index = ap.evalXPath();
				if (index != -1)
					result.add(navigator.toString(index).trim());
				else
					break;
			}		
		} catch (Exception e) {
			throw new Exception("Errors while performing xpath operation:" + xpath, e);
		}
		
		return result;
	}
}
