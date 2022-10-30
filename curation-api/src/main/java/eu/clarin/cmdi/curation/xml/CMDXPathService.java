package eu.clarin.cmdi.curation.xml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CMDXPathService {

   private VTDNav navigator = null;

   public CMDXPathService(Path path) throws IOException, ParseException {
      VTDGen parser = new VTDGen();

      parser.setDoc(Files.readAllBytes(path));
      parser.parse(false);
      navigator = parser.getNav();
      parser = null;

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
         AutoPilot ap = new AutoPilot(reset());
         ap.selectXPath(xpath);
         int index = ap.evalXPath();
         if (index != -1)
            result = navigator.toString(index).trim();
      }
      catch (Exception e) {
         throw new Exception("Errors while performing xpath operation:" + xpath, e);
      }
      return result;
   }

   public String getXPathAttrValue(String xpath, String attributeName) throws Exception {
      String result = null;
      try {
         AutoPilot ap = new AutoPilot(reset());
         ap.selectXPath(xpath);
         int index = ap.evalXPath();
         if (index != -1)
            result = navigator.toString(navigator.getAttrVal(attributeName)).trim();
      }
      catch (Exception e) {
         throw new Exception("Errors while performing xpath operation:" + xpath, e);
      }
      return result;
   }

   public Collection<String> getXPathValues(String xpath) throws Exception{

      Collection<String> result = new ArrayList<>();

         AutoPilot ap = new AutoPilot(reset());
         try {
            ap.selectXPath(xpath);
            while (true) {
               int index = ap.evalXPath();
               if (index != -1)
                  result.add(navigator.toString(index).trim());
               else
                  break;
            }
         }
         catch (XPathParseException e) {
            
            log.error("can't parse xpath '{}'", xpath);
            throw e;
         }
         catch (XPathEvalException e) {
            
            log.error("can't evaluate xpath '{}'", xpath);
            throw e;
         }
         catch (NavException e) {
            
            log.error("can't navigate in document");
            throw e;
         }



      return result;
   }

   public VTDNav reset() throws Exception {
      try {
         navigator.toElement(VTDNav.ROOT);
      }
      catch (NavException e) {
         
         log.error("NaxException at reset");
         throw e;
      
      }
      return navigator;
   }
}
