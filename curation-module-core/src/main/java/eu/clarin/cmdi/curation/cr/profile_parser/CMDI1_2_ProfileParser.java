package eu.clarin.cmdi.curation.cr.profile_parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;

import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;

class CMDI1_2_ProfileParser extends ProfileParser{
	
	static final String ENVELOPE_URL = "https://infra.clarin.eu/CMDI/1.2/xsd/cmd-envelop.xsd";
	
	private static Map<String, String> envelope = null;

	@Override
	protected String getCMDVersion() {
		return "1.2";
	}	
	
	@Override
	protected String conceptAttributeName(){
		return "ConceptLink";
	}	
	
	@Override
	protected CRElement processNameAttributeNode() throws VTDException {
		//ref attribute
		String ref = extractAttributeValue("ref");
		if(ref != null){			
			switch(ref){//we care only about cmd:ComponentId
				case "cmd:ComponentId":
					CRElement elem = new CRElement();
					elem.lvl = vn.getCurrentDepth();
					elem.type = NodeType.COMPONENT;
					elem.ref = extractAttributeValue("fixed");
					return elem;
				default:
					return null;
			}
		}
		
		//name attribute
		String name = extractAttributeValue("name");
		if(name != null){
			String concept = extractAttributeValue("ConceptLink");
			if(concept == null)
				return null;
			CRElement elem = new CRElement();
			elem.lvl = vn.getCurrentDepth();
			elem.type = NodeType.ATTRIBUTE;
			elem.ref = 	concept;
			elem.name = name;
			return elem;
		}		
		return null;		
	}
	
	@Override
	protected Map<String, String> createMap(Collection<CRElement> nodes) throws VTDException {
		Map<String, String> map = new HashMap<>();
		nodes.stream().forEach(node -> {
			String xpath = "";
			CRElement parent = node.parent;
			while (parent != null) {
				xpath = parent.name + "/" + xpath;
				parent = parent.parent;
			}
			xpath = "/CMD/Components/" + xpath + (node.type == NodeType.ATTRIBUTE || node.type == NodeType.CMD_VERSION_ATTR
					? "@" + node.name : node.name + "/text()");

			map.put(xpath, node.getConcept());

		});
		
		//add xpaths from envelope
		if(envelope == null){
			CMDI1_1_ProfileParser envelopeParser = new CMDI1_1_ProfileParser();
			VTDGen vg = new VTDGen();
			vg.parseHttpUrl(ENVELOPE_URL, false);
			envelope = envelopeParser.parse(vg.getNav(), new ProfileHeader()).xpathConceptMap;
		}
		map.putAll(envelope);

		return map;
	}

}
