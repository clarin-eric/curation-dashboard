package eu.clarin.cmdi.curation.cr.profile_parser;

public class CMDINode{
	
	public boolean isRequired;
	public Concept concept;
	public Component component;
	
	@Override
    public String toString() {
    	return (component != null? component.toString() : concept != null? concept.toString() : "");
    }
	
	public static class Concept{		
		
		public String uri;
		public String prefLabel;
		public String status;
		
		public Concept(String uri, String prefLabel, String status) {
			this.uri = uri;
			this.prefLabel = prefLabel;
			this.status = status;
		}


		@Override
		public String toString() {
			return prefLabel + ": " + uri + "\tstatus: " + status;
		}
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
