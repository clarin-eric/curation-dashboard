import eu.clarin.curation.linkchecker.httpLinkChecker.HTTPLinkChecker;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class UrlManipulatorTest {

    @Test
    public void relativeRedirectTestShouldReturnCorrectly() throws URISyntaxException {
        HTTPLinkChecker linkChecker = new HTTPLinkChecker();

        String origin = "https://dgd.ids-mannheim.de/DGD2Web/ExternalAccessServlet?command=displayData&id=ZW--_E_04172_SE_01_A_01_DF_01";
        String location = "./jsp/ExternalLogin.jsp?remember=ID79BE876A-7A1D-A441-44E9-976FEBE4748F";
        String result = "https://dgd.ids-mannheim.de/DGD2Web/jsp/ExternalLogin.jsp?remember=ID79BE876A-7A1D-A441-44E9-976FEBE4748F";
        assertEquals(result,linkChecker.convertRelativeToAbsolute(origin,location));

        origin = "https://d-nb.info/gnd/141535091";
        location = "/gnd/141535091/about/html";
        result = "https://d-nb.info/gnd/141535091/about/html";
        assertEquals(result,linkChecker.convertRelativeToAbsolute(origin,location));

        origin = "https://d-nb.info/gnd/141535091/about/html";
        location = "https://portal.dnb.de/opac.htm?method=simpleSearch&cqlMode=true&query=nid%3D141535091";
        result = "https://portal.dnb.de/opac.htm?method=simpleSearch&cqlMode=true&query=nid%3D141535091";
        assertEquals(result,linkChecker.convertRelativeToAbsolute(origin,location));
    }


}
