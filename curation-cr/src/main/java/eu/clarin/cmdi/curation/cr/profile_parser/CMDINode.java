package eu.clarin.cmdi.curation.cr.profile_parser;

import java.util.Objects;

import eu.clarin.cmdi.curation.ccr.CCRConcept;

public class CMDINode{
	
	public boolean isRequired;
	public CCRConcept concept;
	public Component component;
	
	@Override
    public String toString() {
    	return (component != null? component.toString() : concept != null? concept.toString() : "");
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CMDINode cmdiNode = (CMDINode) o;
		return isRequired == cmdiNode.isRequired &&
				Objects.equals(concept, cmdiNode.concept) &&
				Objects.equals(component, cmdiNode.component);
	}

	@Override
	public int hashCode() {
		return Objects.hash(isRequired, concept, component);
	}

	public static class Component{
		public String name;
		public String id;
		
		public Component(String name, String id) {
			this.name = name;
			this.id = id;
		}

		@Override
		public String toString() {
			return "component: " + name + " / " + id;
		}
	}
	
}
