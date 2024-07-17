package eu.clarin.cmdi.curation.cr.profile_parser;

import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode.Component;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The type Cmdi 1 1 profile parser.
 */
@Service
public class CMDI1_1_ProfileParser extends ProfileParser{

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
	protected CRElement processNameAttributeNode(VTDNav vn) throws VTDException {
		String name = extractAttributeValue(vn,"name");
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
				elem.ref = extractAttributeValue(vn,"fixed");
				break;
			default: // attribute
				elem.type = NodeType.ATTRIBUTE;
				elem.ref = extractAttributeValue(vn, conceptAttributeName());
				elem.name = name;				
		}
		
		return elem;
	}

	/**
	 * Create parsed profile
	 *
	 * @param nodes  the nodes
	 * @return the parsed profile
	 * @throws VTDException the vtd exception
	 */
	@Override
	protected void fillMaps(Collection<CRElement> nodes, Map<String, CMDINode> xpathNode, Map<String, CMDINode> xpathElementNode, Map<String, CMDINode> xpathComponentNode) throws CCRServiceNotAvailableException {

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
	}
}
