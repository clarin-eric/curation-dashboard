package eu.clarin.cmdi.curation.cr.profile_parser;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ximpleware.VTDException;

import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode.Component;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;

class CMDI1_1_ProfileParser extends ProfileParser{
	

	@Override
	protected String getCMDVersion() {
		return "1.1";
	}
	
	@Override
	protected String conceptAttributeName(){
		return "datcat";
	}		

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
	
	@Override
	protected Map<String, CMDINode> createMap(Collection<CRElement> nodes) throws VTDException {
		Map<String, CMDINode> xpaths = new LinkedHashMap<>();
		
		nodes.stream()
		.filter(n -> n.isLeaf || n.type == NodeType.COMPONENT)
		.forEach(node -> {
			String xpath = "";
			CRElement parent = node.parent;
			while (parent != null) {
				xpath = parent.name + "/" + xpath;
				parent = parent.parent;
			}
			xpath = "/" + xpath + (node.type == NodeType.ATTRIBUTE || node.type == NodeType.CMD_VERSION_ATTR
					? "@" + node.name : node.name + "/text()");
			
			CMDINode cmdiNode = new CMDINode();
			cmdiNode.isRequired = node.isRequired;
			
			if(node.type == NodeType.COMPONENT)
				cmdiNode.component = new Component(node.name, node.ref);				
			else
				cmdiNode.concept = createConcept(node.ref);

			xpaths.put(xpath, cmdiNode);

		});

		return xpaths;
	}

}
