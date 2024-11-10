package eu.clarin.cmdi.curation.cr.profile_parser;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.curation.ccr.CCRConcept;
import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.ccr.CCRStatus;
import eu.clarin.cmdi.curation.ccr.conf.CCRConfig;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * The type Profile parser.
 */
@Slf4j
public abstract class ProfileParser {

   private final Pattern CR_REST_PATTERN;
   /**
    * The Ccr service.
    */
   protected CCRService ccrService;

   private CRElement _cur;



   /**
    * Instantiates a new Profile parser.
    *
    * @param ccrService the ccr service
    */
   public ProfileParser(CCRService ccrService, CRConfig crConfig) {
      
      this.ccrService = ccrService;

      String CR_REST = crConfig.getRestApi().replaceFirst("http(s)?", "(http|https)").replaceFirst("/1\\..+", "/.+");

      this.CR_REST_PATTERN = Pattern.compile(CR_REST);
   }

   /**
    * Parse parsed profile.
    *
    * @param vn the navigator
    * @param schemaLocation the schema location
    * @param header public schema header or null
    * @return the parsed profile
    * @throws CCRServiceNotAvailableException
    * @throws CRServiceStorageException
    */
   public ParsedProfile parse(VTDNav vn, String schemaLocation, ProfileHeader header) throws CCRServiceNotAvailableException, CRServiceStorageException {

      try {

         if(header == null){
            header = getHeader(vn, schemaLocation);
         }

         Collection<CRElement> nodes = processElements(vn);

         Map<String, CMDINode> xpathNode = new LinkedHashMap<>();

         Map<String, CMDINode> xpathElementNode = new LinkedHashMap<>();

         Map<String, CMDINode> xpathComponentNode = new LinkedHashMap<>();

         fillMaps(nodes, xpathNode, xpathElementNode, xpathComponentNode);


         return new ParsedProfile(
                 header,
                 xpathNode,
                 xpathElementNode,
                 xpathComponentNode);
      }
      catch (VTDException e) {

         log.info("profile not parsable!");
         log.debug("", e);
      }

      return null;
   }


   /**
    * Gets cmd version.
    *
    * @return the cmd version
    */
   protected abstract String getCMDVersion();

   /**
    * Concept attribute name string.
    *
    * @return the string
    */
   protected abstract String conceptAttributeName();

   /**
    * Process name attribute node cr element.
    *
    * @return the cr element
    * @throws VTDException the vtd exception
    */
   protected abstract CRElement processNameAttributeNode(VTDNav vn) throws VTDException;

   /**
    * fill the maps
    *
    * @param nodes  the nodes
    * @param xpathNode
    * @param xpathElementNode
    * @param xpathComponentNode
    * @return the parsed profile
    * @throws CCRServiceNotAvailableException
    * @throws CRServiceStorageException
    */
   protected abstract void fillMaps(Collection<CRElement> nodes, Map<String, CMDINode> xpathNode, Map<String, CMDINode> xpathElementNode, Map<String, CMDINode> xpathComponentNode) throws CCRServiceNotAvailableException, CRServiceStorageException;

   /**
    * Fill in header profile header.
    *
    * @param vn     the vn
    * @throws VTDException the vtd exception
    */
   protected eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader getHeader(VTDNav vn, String schemaLocation) throws VTDException {

      AutoPilot ap = new AutoPilot(vn);
      ap.declareXPathNameSpace("cmd", "http://www.clarin.eu/cmd/1");
      ap.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");

      return new ProfileHeader(
         this.getCMDVersion(),
         schemaLocation,
         evaluateXPath(vn,"//cmd:Header/cmd:ID/text()", ap),
         evaluateXPath(vn,"//cmd:Header/cmd:Name/text()", ap),
         evaluateXPath(vn, "//cmd:Header/cmd:Description/text()", ap),
         evaluateXPath(vn,"//cmd:Header/cmd:Status/text()", ap),
        false,
         isCrResident(schemaLocation)
      );
   }

   private Collection<CRElement> processElements(VTDNav vn) throws VTDException {
      Collection<CRElement> nodes = new ArrayList<>();
      vn.toElement(VTDNav.ROOT);// reset
      AutoPilot ap = new AutoPilot(vn);

      ap.selectElement("xs:element");
      while (ap.iterate()) {// process single element
         CRElement _new = new CRElement();
         _new.isLeaf = !isComplex(vn);

         _new.name = extractAttributeValue(vn, "name");
         if (_new.name == null) {
            _new.name = "";
            log.error("Element at {} doesn't have specified name, xpaths will be invalid", vn.getCurrentIndex());
         }

         String minOccurs = extractAttributeValue(vn,"minOccurs");
         _new.isRequired = minOccurs == null || !minOccurs.equals("0");

         _new.lvl = vn.getCurrentDepth();

         // getAttrs
         vn.push();
         Collection<CRElement> attributes = getAttributes(vn);
         vn.pop();
         CRElement component = attributes.stream().filter(attr -> attr.type == NodeType.COMPONENT).findAny()
               .orElse(null);

         if (component != null) { // component
            _new.type = NodeType.COMPONENT;
            _new.ref = component.ref;
         }
         else {
            _new.type = NodeType.ELEMENT;
            _new.ref = extractAttributeValue(vn, conceptAttributeName());
         }

         // resolve parent
         if (_cur != null) {// if not root
            if (_new.lvl > _cur.lvl)
               _new.parent = _cur;
            else if (_new.lvl == _cur.lvl)
               _new.parent = _cur.parent;
            else {// child.lvl < lvl -> sibling of parent('s parent)
               CRElement parent = _cur.parent;
               while (parent != null && _new.lvl <= parent.lvl)
                  parent = parent.parent;
               _new.parent = parent;
            }
         }
         _cur = _new;
         nodes.add(_new);

         // add attribute nodes
         attributes.stream().filter(attr -> attr.type == NodeType.ATTRIBUTE || attr.type == NodeType.CMD_VERSION_ATTR)
               .forEach(attr -> {
                  attr.parent = _new;
                  nodes.add(attr);
               });
      }
      return nodes;
   }

   private boolean isComplex(VTDNav vn) throws VTDException {
      vn.push();
      AutoPilot ap = new AutoPilot(vn);
      ap.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");
      ap.selectXPath("./xs:complexType/xs:sequence");
      boolean isComplex = ap.evalXPathToBoolean();
      vn.pop();
      return isComplex;
   }

   private Collection<CRElement> getAttributes(VTDNav vn) throws VTDException {
      Collection<CRElement> attributes = new ArrayList<>();

      vn.push();
      AutoPilot ap = new AutoPilot(vn);
      ap.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");
      ap.selectXPath("./xs:complexType/xs:simpleContent/xs:extension/xs:attribute");
      while (ap.evalXPath() != -1) {
         CRElement nameAttr = processNameAttributeNode(vn);
         if (nameAttr != null)
            attributes.add(nameAttr);
      }
      vn.pop();

      // teiHeader.xsd has attributes on different xpath
      if (attributes.isEmpty()) {// paths are exclusive
         ap.bind(vn);
         ap.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");
         ap.selectXPath("./xs:complexType/xs:attribute");
         while (ap.evalXPath() != -1) {
            CRElement nameAttr = processNameAttributeNode(vn);
            if (nameAttr != null)
               attributes.add(nameAttr);
         }
      }
      return attributes;
   }

   /**
    * Evaluate x path string.
    *
    * @param xpath the xpath
    * @param ap    the ap
    * @return the string
    * @throws VTDException the vtd exception
    */
// utils
   protected String evaluateXPath(VTDNav vn, String xpath, AutoPilot ap) throws VTDException {
      ap.selectXPath(xpath);
      int index = ap.evalXPath();
      return index != -1 ? vn.toString(index).trim() : null;
   }

   /**
    * Extract attribute value string.
    *
    * @param attrName the attr name
    * @return the string
    * @throws NavException the nav exception
    */
   protected String extractAttributeValue(VTDNav vn, String attrName) throws NavException {
      int ind = vn.getAttrVal(attrName);
      return ind != -1 ? vn.toNormalizedString(ind) : null;
   }

   /**
    * Create concept ccr concept.
    *
    * @param uri the uri
    * @return the ccr concept
    */
   protected CCRConcept createConcept(String uri) throws CCRServiceNotAvailableException {

      if (uri == null)
         return null;
      CCRConcept concept;
      if (uri.startsWith("http://purl.org/dc/terms/")) {

         concept = new CCRConcept(uri, uri.substring("http://purl.org/dc/terms/".length()), CCRStatus.DUBLIN_CORE);
      }
      else {

         CCRConcept ccrConcept = null;

         ccrConcept = ccrService.getConcept(uri);


          concept = Objects.requireNonNullElseGet(ccrConcept, () -> new CCRConcept(uri, "invalid concept", CCRStatus.UNKNOWN));
      }

      return concept;
   }
   private boolean isCrResident(String schemaLocation) {

      return CR_REST_PATTERN.matcher(schemaLocation).matches();
   }
}
