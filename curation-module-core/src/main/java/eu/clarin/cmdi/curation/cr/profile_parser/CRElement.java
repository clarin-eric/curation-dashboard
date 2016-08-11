package eu.clarin.cmdi.curation.cr.profile_parser;

public class CRElement {
	
	public enum NodeType {COMPONENT, ELEMENT, ATTRIBUTE, CMD_VERSION_ATTR};
	
	String name = null;
	String ref = null; //datcat or id in case of component
	
	boolean isRequired;	
	NodeType type;
	int lvl;
	CRElement parent = null;
	
	
	public String getName() {
		return name;
	}
	public boolean isRequired() {
		return isRequired;
	}
	
	public NodeType getType(){
		return type;
	}
	
	public String getConcept(){//
		return type == NodeType.ELEMENT || type == NodeType.ATTRIBUTE? ref : null;
	}
	
	public String getComponentId(){//if node is not component returns null
		return type == NodeType.COMPONENT? ref : null;
	}
	
	@Override
	public String toString() {
		return type.name() + "\t" + name + "\t" + ref;
	}
	

}
