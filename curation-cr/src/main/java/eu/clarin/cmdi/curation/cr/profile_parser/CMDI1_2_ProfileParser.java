package eu.clarin.cmdi.curation.cr.profile_parser;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;

import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode.Component;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;

class CMDI1_2_ProfileParser extends ProfileParser {
   
   private static final String ENVELOPE_URL = "https://infra.clarin.eu/CMDI/1.2/xsd/cmd-envelop.xsd";

   private static ParsedProfile envelope;

   public CMDI1_2_ProfileParser(CCRService ccrService) {

      super(ccrService);

   }

   @Override
   protected String getCMDVersion() {
      return "1.2";
   }

   @Override
   protected String conceptAttributeName() {
      return "cmd:ConceptLink";
   }

   @Override
   protected CRElement processNameAttributeNode() throws VTDException {
      // ref attribute
      String ref = extractAttributeValue("ref");
      if (ref != null) {
         switch (ref) {// we care only about cmd:ComponentId
         case "cmd:ComponentId":
            CRElement elem = new CRElement();
            elem.isLeaf = true;
            elem.lvl = vn.getCurrentDepth();
            elem.type = NodeType.COMPONENT;
            elem.ref = extractAttributeValue("fixed");
            return elem;
         default:
            return null;
         }
      }

      // name attribute
      String name = extractAttributeValue("name");
      if (name != null) {
         String concept = extractAttributeValue("cmd:ConceptLink");
         CRElement elem = new CRElement();
         elem.isLeaf = true;
         elem.lvl = vn.getCurrentDepth();
         elem.type = NodeType.ATTRIBUTE;
         elem.ref = concept;
         elem.name = name;
         return elem;
      }

      return null;
   }

   @Override
   protected ParsedProfile createParsedProfile(ProfileHeader header, Collection<CRElement> nodes)
         throws VTDException, NoProfileCacheEntryException {

      Map<String, CMDINode> xpathNode = new LinkedHashMap<>();

      Map<String, CMDINode> xpathElementNode = new LinkedHashMap<>();

      Map<String, CMDINode> xpathComponentNode = new LinkedHashMap<>();


      // add xpaths from envelope
      if (envelope == null) {
         CMDI1_1_ProfileParser envelopeParser = new CMDI1_1_ProfileParser(ccrService);
         VTDGen vg = new VTDGen();
         
         vg.parseHttpUrl(ENVELOPE_URL, true);

         envelope = envelopeParser.parse(vg.getNav(), new ProfileHeader());
      }
      
      xpathNode.putAll(envelope.getXpathNode());
      xpathElementNode.putAll(envelope.getXpathElementNode());
      xpathComponentNode.putAll(envelope.getXpathComponentNode());

      nodes.stream().filter(n -> n.isLeaf || n.type == NodeType.COMPONENT).forEach(node -> {
         String xpath = "";
         CRElement parent = node.parent;
         while (parent != null) {
            xpath = "cmdp:" + parent.name + "/" + xpath;
            parent = parent.parent;
         }
         xpath = "/cmd:CMD/cmd:Components/" + xpath
               + (node.type == NodeType.ATTRIBUTE || node.type == NodeType.CMD_VERSION_ATTR ? "@" + node.name
                     : "cmdp:" + node.name + "/text()");

         CMDINode cmdiNode = new CMDINode();
         cmdiNode.isRequired = node.isRequired;

         if (node.type == NodeType.COMPONENT) {

            cmdiNode.component = new Component(node.name, node.ref);

            xpathComponentNode.put(xpath, cmdiNode);
         }
         else {
            
            cmdiNode.concept = createConcept(node.ref);

            xpathElementNode.put(xpath, cmdiNode);
         }

         xpathNode.put(xpath, cmdiNode);

      });

      return new ParsedProfile(
            header, 
            xpathNode, 
            xpathElementNode, 
            xpathComponentNode
         );
   }
}
