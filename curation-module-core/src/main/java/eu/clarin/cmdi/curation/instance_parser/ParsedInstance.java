package eu.clarin.cmdi.curation.instance_parser;

import java.util.Collection;

public class ParsedInstance {
	
	private final Collection<InstanceNode> nodes;
	
	public ParsedInstance(Collection<InstanceNode> nodes) {
		this.nodes = nodes;
	}	

	public Collection<InstanceNode> getNodes() {
		return nodes;
	}

	public static class InstanceNode{
		private final String xpath;
		private final String value;
		
		public InstanceNode(String xpath, String value) {
			//remove namespaces from xpath
			//xpath = xpath.replaceAll("cmd:", "").replaceAll("cmdp:", "");
			
			this.xpath = xpath;
			this.value = value;
		}
		
		public String getXpath() {
			return xpath;
		}
		public String getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return xpath + "\t" + value;
		}
	}
}
