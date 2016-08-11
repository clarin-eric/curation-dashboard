package eu.clarin.cmdi.curation.cr.profile_parser;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.cr.profile_parser.CRElement.NodeType;

public class ParsedProfile {
	
	final ProfileHeader header;
	
	final Collection<CRElement> nodes;
	final Map<String, String> xpathConceptMap;

	
	ParsedProfile(ProfileHeader header, Collection<CRElement> children, Map<String, String> xpathConceptMap){
		this.header = header;
		this.nodes = children;
		this.xpathConceptMap = xpathConceptMap;
	}

	public String getId(){
		return header.id;
	}
	
	public String getName(){
		return header.name;
	}
	
	public String getDescription(){
		return header.description;
	}
	
	public String getSchemaLocation(){
		return header.schemaLocation;
	}
	
	public String getCMDIVersion(){
		return header.cmdiVersion;
	}
	
	public boolean isPublic(){
		return header.isPublic;
	}
	
	public Collection<CRElement> getNodes() {
		return nodes;
	}
	
	public String getCMDVersion(){
		CRElement cmdVer = nodes.stream().filter(node -> node.type == NodeType.CMD_VERSION_ATTR).findFirst().orElse(null);
		return cmdVer != null? cmdVer.ref : "not specified";
	}

	public Map<String, String> getXpathConceptMap() {
		return xpathConceptMap;
	}
	
	public Collection<CRElement> getComponents(){
		return nodes
				.stream()
				.filter(node -> node.type == NodeType.COMPONENT)
				.collect(Collectors.toList());
	}
	
	public Collection<CRElement> getElements(){
		return nodes
				.stream()
				.filter(node -> node.type != NodeType.COMPONENT)
				.collect(Collectors.toList());
	}
	
	public Collection<String> getXPaths(){		
		return xpathConceptMap.keySet();
	}
	
	public Collection<String> getXPathsForConcept(String concept){
		return xpathConceptMap.entrySet()
				.stream()
				.filter(entry -> 
					entry.getValue() == null?
						 false : concept.equals(entry.getValue())
				)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}
	
	public String getConcept(String xpath){
		return xpathConceptMap.get(xpath);
	}
	
	
	//xpath will be pre-processed -> remove conditions, text()
	public boolean hasXPath(String xpath){
		final String normalisedXPath = preprocessXPath(xpath);
		if(normalisedXPath.contains("//")){//handle generic xpaths
			return xpathConceptMap.keySet().stream()
			.filter(entry -> {
				String[] chunks = normalisedXPath.split("//");
				int curInd = 0;
				for(int i= 0; i < chunks.length; i++){
					if(curInd > entry.length())
						return false;			
					if(i == 0 && !chunks[0].isEmpty()){
						if(entry.startsWith(chunks[i])){
							curInd += chunks[0].length() + 1; // first after '/'					
							continue;
						}else
							return false;
					}else{
						curInd = entry.indexOf(chunks[i], curInd);
						if(curInd == -1)
							return false;
						curInd += chunks[i].length() + 1; // first after '/'				
					}
				}						
				return true;						
			})
			.findFirst() //match first xpath in the map
			.orElse(null) //if no match return null
			!= null? true : false;			
		}else
			return xpathConceptMap.containsKey(normalisedXPath);
	}
	
	private String preprocessXPath(String xpath){		
		//remove conditions
		xpath = xpath.replaceAll("\\[.*?\\]", "");		
		//remove text()
//		xpath = xpath.replace("/text()", "");
		//remove attributes
		xpath = xpath.replaceAll("/@.*", "/");		
		return xpath;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CMD Version: " + getCMDVersion()).append("\n");
		sb.append(header.id).append("\t").append(header.name).append("\t").append(header.description).append("\n");
		sb.append("Nodes:").append("\n");
		nodes.forEach(n -> sb.append("\t").append(n).append("\n"));
		
		sb.append("\n").append("xpaths:").append("\n");
		xpathConceptMap.entrySet().forEach(e -> sb.append("\t").append(e.getKey() + ": " + e.getValue()).append("\n"));
		
		return sb.toString();
	}

}
