package eu.clarin.cmdi.curation.cr.profile_parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.ccr.CCRConcept;
import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.cr.exception.NoParsedProfileException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode.Concept;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ProfileParser {

   protected CCRService ccrService;
   
   public ProfileParser(CCRService ccrService) {
      
      this.ccrService = ccrService;
   }

   protected VTDNav vn;

   private CRElement _cur;

   public ParsedProfile parse(VTDNav navigator, ProfileHeader header) throws NoParsedProfileException{
      vn = navigator;
      try {
         fillInHeader(vn, header);
         Collection<CRElement> nodes = processElements();
         return new ParsedProfile(header, createMap(nodes));
      }
      catch (VTDException e) {
         
         log.error("profile not parsable!");
         log.debug("", e);
         throw new NoParsedProfileException();
      }
   }

   protected abstract String getCMDVersion();

   protected abstract String conceptAttributeName();

   protected abstract CRElement processNameAttributeNode() throws VTDException;

   protected abstract Map<String, CMDINode> createMap(Collection<CRElement> nodes) throws VTDException, NoParsedProfileException;

   protected ProfileHeader fillInHeader(VTDNav vn, ProfileHeader header) throws VTDException {
      AutoPilot ap = new AutoPilot(vn);
      ap.declareXPathNameSpace("cmd", "http://www.clarin.eu/cmd/1");
      ap.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");

      header.setId(evaluateXPath("//cmd:Header/cmd:ID/text()", ap));
      header.setName(evaluateXPath("//cmd:Header/cmd:Name/text()", ap));
      header.setDescription(evaluateXPath("//cmd:Header/cmd:Description/text()", ap));
      header.setStatus(evaluateXPath("//cmd:Header/cmd:Status/text()", ap));
      header.setCmdiVersion(getCMDVersion());
      return header;

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
         _new.isRequired = minOccurs != null ? !minOccurs.equals("0") : true;

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

   // utils
   protected String evaluateXPath(String xpath, AutoPilot ap) throws VTDException {
      ap.selectXPath(xpath);
      int index = ap.evalXPath();
      return index != -1 ? vn.toString(index).trim() : null;
   }

   protected String extractAttributeValue(String attrName) throws NavException {
      int ind = vn.getAttrVal(attrName);
      return ind != -1 ? vn.toNormalizedString(ind) : null;
   }

   protected Concept createConcept(String uri){

      if (uri == null)
         return null;
      Concept concept;
      if (uri.startsWith("http://purl.org/dc/terms/")) {
         concept = new Concept(uri, uri.substring("http://purl.org/dc/terms/".length()), "DC");
      }
      else {
         CCRConcept ccrConcept = ccrService.getConcept(uri);


         if (ccrConcept != null)
            concept = new Concept(uri, ccrConcept.getPrefLabel(), ccrConcept.getStatus().toString());
         else
            concept = new Concept(uri, "invalid concept", "???");
      }

      return concept;
   }
}
