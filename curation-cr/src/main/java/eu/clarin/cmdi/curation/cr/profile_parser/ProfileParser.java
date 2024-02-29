package eu.clarin.cmdi.curation.cr.profile_parser;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.curation.ccr.CCRConcept;
import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.ccr.CCRStatus;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * The type Profile parser.
 */
@Slf4j
public abstract class ProfileParser {

   /**
    * The Ccr service.
    */
   protected CCRService ccrService;

   /**
    * The Vn.
    */
   protected VTDNav vn;

   private CRElement _cur;

   /**
    * Instantiates a new Profile parser.
    *
    * @param ccrService the ccr service
    */
   public ProfileParser(CCRService ccrService) {
      
      this.ccrService = ccrService;
   }

   /**
    * Parse parsed profile.
    *
    * @param navigator the navigator
    * @param profileHeader the profile header
    * @return the parsed profile
    * @throws NoProfileCacheEntryException the no profile cache entry exception
    */
   public ParsedProfile parse(VTDNav navigator, ProfileHeader profileHeader) throws NoProfileCacheEntryException{
      vn = navigator;
      try {
         fillInHeader(vn, profileHeader);
         Collection<CRElement> nodes = processElements();
         return createParsedProfile(profileHeader, nodes);
      }
      catch (VTDException e) {
         
         log.error("profile not parsable!");
         log.debug("", e);
         throw new NoProfileCacheEntryException();
      }
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
   protected abstract CRElement processNameAttributeNode() throws VTDException;

   /**
    * Create parsed profile parsed profile.
    *
    * @param profileHeader the profile header
    * @param nodes  the nodes
    * @return the parsed profile
    * @throws VTDException                 the vtd exception
    * @throws NoProfileCacheEntryException the no profile cache entry exception
    */
   protected abstract ParsedProfile createParsedProfile(ProfileHeader profileHeader, Collection<CRElement> nodes) throws VTDException, NoProfileCacheEntryException;

   /**
    * Fill in header profile header.
    *
    * @param vn     the vn
    * @param profileHeader the profile header
    * @throws VTDException the vtd exception
    */
   protected void fillInHeader(VTDNav vn, ProfileHeader profileHeader) throws VTDException {

      AutoPilot ap = new AutoPilot(vn);
      ap.declareXPathNameSpace("cmd", "http://www.clarin.eu/cmd/1");
      ap.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");

      profileHeader.setId(evaluateXPath("//cmd:Header/cmd:ID/text()", ap));
      profileHeader.setName(evaluateXPath("//cmd:Header/cmd:Name/text()", ap));
      profileHeader.setDescription(evaluateXPath("//cmd:Header/cmd:Description/text()", ap));
      profileHeader.setStatus(evaluateXPath("//cmd:Header/cmd:Status/text()", ap));
      profileHeader.setCmdiVersion(getCMDVersion());
   }

   private Collection<CRElement> processElements() throws VTDException {
      Collection<CRElement> nodes = new ArrayList<>();
      vn.toElement(VTDNav.ROOT);// reset
      AutoPilot ap = new AutoPilot(vn);

      ap.selectElement("xs:element");
      while (ap.iterate()) {// process single element
         CRElement _new = new CRElement();
         _new.isLeaf = !isComplex();

         _new.name = extractAttributeValue("name");
         if (_new.name == null) {
            _new.name = "";
            log.error("Element at {} doesn't have specified name, xpaths will be invalid", vn.getCurrentIndex());
         }

         String minOccurs = extractAttributeValue("minOccurs");
         _new.isRequired = minOccurs == null || !minOccurs.equals("0");

         _new.lvl = vn.getCurrentDepth();

         // getAttrs
         vn.push();
         Collection<CRElement> attributes = getAttributes();
         vn.pop();
         CRElement component = attributes.stream().filter(attr -> attr.type == NodeType.COMPONENT).findAny()
               .orElse(null);

         if (component != null) { // component
            _new.type = NodeType.COMPONENT;
            _new.ref = component.ref;
         }
         else {
            _new.type = NodeType.ELEMENT;
            _new.ref = extractAttributeValue(conceptAttributeName());
         }

         // resolve parent
         if (_cur != null) {// if not root
            if (_new.lvl > _cur.lvl)
               _new.parent = _cur;
            else if (_new.lvl == _cur.lvl)
               _new.parent = _cur.parent;
            else {// child.lvl < lvl -> sibling of parent('s parent)
               CRElement parent = _cur.parent;
               while (_new.lvl <= parent.lvl)
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

   private boolean isComplex() throws VTDException {
      vn.push();
      AutoPilot ap = new AutoPilot(vn);
      ap.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");
      ap.selectXPath("./xs:complexType/xs:sequence");
      boolean isComplex = ap.evalXPathToBoolean();
      vn.pop();
      return isComplex;
   }

   private Collection<CRElement> getAttributes() throws VTDException {
      Collection<CRElement> attributes = new ArrayList<>();

      vn.push();
      AutoPilot ap = new AutoPilot(vn);
      ap.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");
      ap.selectXPath("./xs:complexType/xs:simpleContent/xs:extension/xs:attribute");
      while (ap.evalXPath() != -1) {
         CRElement nameAttr = processNameAttributeNode();
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
            CRElement nameAttr = processNameAttributeNode();
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
   protected String evaluateXPath(String xpath, AutoPilot ap) throws VTDException {
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
   protected String extractAttributeValue(String attrName) throws NavException {
      int ind = vn.getAttrVal(attrName);
      return ind != -1 ? vn.toNormalizedString(ind) : null;
   }

   /**
    * Create concept ccr concept.
    *
    * @param uri the uri
    * @return the ccr concept
    */
   protected CCRConcept createConcept(String uri) {

      if (uri == null)
         return null;
      CCRConcept concept;
      if (uri.startsWith("http://purl.org/dc/terms/")) {

         concept = new CCRConcept(uri, uri.substring("http://purl.org/dc/terms/".length()), CCRStatus.DUBLIN_CORE);
      }
      else {

         CCRConcept ccrConcept = null;

         try {
            ccrConcept = ccrService.getConcept(uri);
         }
         catch(CCRServiceNotAvailableException ex){

            log.error("CCRService not available. Setting default concept for URI '{}'", uri);
         }

          concept = Objects.requireNonNullElseGet(ccrConcept, () -> new CCRConcept(uri, "invalid concept", CCRStatus.UNKNOWN));
      }

      return concept;
   }
}
