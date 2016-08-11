package eu.clarin.cmdi.curation.cr.profile_parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ximpleware.VTDException;

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
				if (elem.ref == null)
					elem = null;// consider only attributes with datcat
		}
		
		return elem;
	}
	
	@Override
	protected Map<String, String> createMap(Collection<CRElement> nodes) {
		Map<String, String> map = new HashMap<>();
		nodes.stream().forEach(node -> {
			String xpath = "";
			CRElement parent = node.parent;
			while (parent != null) {
				xpath = parent.name + "/" + xpath;
				parent = parent.parent;
			}
			xpath = "/" + xpath + (node.type == NodeType.ATTRIBUTE || node.type == NodeType.CMD_VERSION_ATTR
					? "@" + node.name : node.name + "/text()");

			map.put(xpath, node.getConcept());

		});

		return map;
	}

}
