package eu.clarin.cmdi.curation.xml;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class CMDIXPathService {

    private static final Logger _logger = LoggerFactory.getLogger(CMDIXPathService.class);

    private VTDNav navigator = null;

    public CMDIXPathService(Path path) throws Exception {
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

    public VTDNav getNavigator() {
	return navigator;
    }
    

    public String xpath(String xpath) throws Exception {
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

}
