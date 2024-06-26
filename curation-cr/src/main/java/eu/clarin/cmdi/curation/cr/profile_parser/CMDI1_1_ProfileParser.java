package eu.clarin.cmdi.curation.cr.profile_parser;

import com.ximpleware.VTDException;
import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode.Component;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;
import eu.clarin.cmdi.curation.pph.ProfileHeader;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The type Cmdi 1 1 profile parser.
 */
class CMDI1_1_ProfileParser extends ProfileParser{

	/**
	 * Instantiates a new Cmdi 1 1 profile parser.
	 *
	 * @param ccrService the ccr service
	 */
	public CMDI1_1_ProfileParser(CCRService ccrService) {
      
	   super(ccrService);
      
   }

	/**
	 * Gets cmd version.
	 *
	 * @return the cmd version
	 */
	@Override
	protected String getCMDVersion() {
		return "1.1";
	}

	/**
	 * Concept attribute name string.
	 *
	 * @return the string
	 */
	@Override
	protected String conceptAttributeName(){
		return "datcat";
	}

	/**
	 * Process name attribute node cr element.
	 *
	 * @return the cr element
	 * @throws VTDException the vtd exception
	 */
	@Override
	protected CRElement processNameAttributeNode() throws VTDException {
		String name = extractAttributeValue("name");
		if(name == null)
			return null;
		
		CRElement elem = new CRElement();
		elem.isLeaf = true;
		elem.lvl = vn.getCurrentDepth();
		
		switch (name) {
			case "ref": 		// ignore
			case "CMDVersion":  // ignore
				elem = null;
				break;
			case "ComponentId":
				elem.type = NodeType.COMPONENT;
				elem.ref = extractAttributeValue("fixed");
				break;
			default: // attribute
				elem.type = NodeType.ATTRIBUTE;
				elem.ref = extractAttributeValue(conceptAttributeName());				
				elem.name = name;				
		}
		
		return elem;
	}

	/**
	 * Create parsed profile
	 *
	 * @param profileHeader the profile header
	 * @param nodes  the nodes
	 * @return the parsed profile
	 * @throws VTDException the vtd exception
	 */
	@Override
	protected ParsedProfile createParsedProfile(ProfileHeader profileHeader, Collection<CRElement> nodes) throws VTDException, CCRServiceNotAvailableException {
		
      Map<String, CMDINode> xpathNode = new LinkedHashMap<>();

      Map<String, CMDINode> xpathElementNode = new LinkedHashMap<>();

      Map<String, CMDINode> xpathComponentNode = new LinkedHashMap<>();

	  	for(CRElement node : nodes){
			  if(node.isLeaf || node.type == NodeType.COMPONENT){
				  String xpath = "";
				  CRElement parent = node.parent;
				  while (parent != null) {
					  xpath = "cmd:" + parent.name + "/" + xpath;
					  parent = parent.parent;
				  }
				  xpath = "/" + xpath + (node.type == NodeType.ATTRIBUTE || node.type == NodeType.CMD_VERSION_ATTR
						  ? "@" + node.name : "cmd:" + node.name + "/text()");

				  CMDINode cmdiNode = new CMDINode();
				  cmdiNode.isRequired = node.isRequired;

				  if(node.type == NodeType.COMPONENT) {
					  cmdiNode.component = new Component(node.name, node.ref);

					  xpathComponentNode.put(xpath, cmdiNode);
				  }
				  else {

					  cmdiNode.concept = createConcept(node.ref);

					  xpathElementNode.put(xpath, cmdiNode);
				  }

				  xpathNode.put(xpath, cmdiNode);

			  }
		}

      return new ParsedProfile(
            profileHeader,
            xpathNode, 
            xpathElementNode, 
            xpathComponentNode
         );
	}
}
