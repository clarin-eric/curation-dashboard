package eu.clarin.cmdi.curation.facets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.utils.Pair;

public class ProfileParser {
	
	public static final String NAMESPACE = "c";

	CMDElement root;
	
	Map<String, String> xpathConceptMap = null;
	
	public ProfileParser(String profile) throws Exception{
		parseProfile(profile);
		xpathConceptMap = root.toMap();
	}
	
	
	public Set<String> getXPaths(){		
		return xpathConceptMap.keySet();
	}
	
	public Set<String> getXPathsForConcept(String concept){
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
		xpath = preprocessXPath(xpath);
		if(xpath.contains("//")){//handle generic xpaths
			String[] chunks = xpath.split("//");
			List<String> filtered = xpathConceptMap.keySet().stream().filter(x -> x.startsWith(chunks[0])).collect(Collectors.toList());
			for(int i = 1; i < chunks.length; i++){
				final String chunk = chunks[i];
				filtered = filtered.stream().filter(x -> x.contains(chunk)).collect(Collectors.toList());
			}
			return !filtered.isEmpty();
			
		}else
			return xpathConceptMap.containsKey(xpath);
	}
	
	public CMDElement getRoot(){
		return root;
	}
	
	public static String preprocessXPath(String xpath){		
		//remove conditions
		xpath = xpath.replaceAll("\\[.*?\\]", "");		
		//remove text()
//		xpath = xpath.replace("/text()", "");
		//remove attributes
		xpath = xpath.replaceAll("/@.*", "/");		
		return xpath;
	}
	
	private CMDElement parseProfile(String profile) throws Exception{
		VTDNav vn = CRService.getInstance().getParsedXSD(profile);
		AutoPilot ap = new AutoPilot(vn);
		
		CMDElement curr = null;
		
		List<Pair<String, Integer>> elems = new ArrayList<>();
		ap.selectElement("element");
		while(ap.iterate()){
			CMDElement elem = processNode(vn, false);
			if(curr == null)
				root = curr = elem;
			else{
				curr = curr.addChild(elem);
				vn.push();//add attributes to the current node
				getAttributes(vn).forEach(curr::addChild); 
				vn.pop();
			}
			
		}
		
		return root;		
	}
	
	private List<CMDElement> getAttributes(VTDNav vn) throws Exception {
		List<CMDElement> attributes = new ArrayList<>();
		
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./complexType/simpleContent/extension/attribute");
		while(ap.evalXPath() != -1){
			CMDElement elem = processNode(vn, true);
			if(elem.concept != null)//consider attrs with concept only
				attributes.add(elem);
		}
		
		//teiHeader.xsd has such a case
		vn.pop();
		ap.bind(vn);
		ap.selectXPath("./complexType/attribute");
		while(ap.evalXPath() != -1){
			CMDElement elem = processNode(vn, true);
			if(elem.concept != null)//consider attrs with concept only
				attributes.add(elem);
		}		
		
		return attributes;		
	}
	
	private CMDElement processNode(VTDNav vn, boolean isAttribute) throws NavException{
		int ind = vn.getAttrVal("name");
		String name = "";
		if(ind != -1)
			name = vn.toNormalizedString(ind);
		return new CMDElement((ind != -1)? vn.toNormalizedString(ind) : "", vn.getCurrentDepth(), getDatcat(vn), isAttribute);
	}
	
	
	private String getDatcat(VTDNav vn) throws NavException {
		int result = -1;
		result = vn.getAttrValNS("http://www.isocat.org/ns/dcr", "datcat");
		if (result == -1) {
			result = vn.getAttrValNS("http://www.isocat.org", "datcat");
		}
		if (result == -1) {
			result = vn.getAttrVal("datcat");
		}
		
		return result != -1? vn.toNormalizedString(result) : null;
	}
	
	
	public static class CMDElement{
		CMDElement parent;
		String name;
		int lvl;
		String concept;
		boolean isAttribute;
		
		List<CMDElement> children = null;
		
		public CMDElement(String name, int lvl, String concept){
			this(name, lvl, concept, false);
		}
		
		public CMDElement(String name, int lvl, String concept, boolean isAttribute){
			this.name = name;
			this.lvl = lvl;
			parent = null;
			this.concept = concept; 
			this.isAttribute = isAttribute;
			children = new ArrayList<>();
		}
		
		public CMDElement addChild(CMDElement child){		
			 if(child.lvl > lvl){//child of current
				 child.parent = this;
				 children.add(child);
			 }else if(child.lvl == lvl){//sibling of current
				 child.parent = this.parent;
				 this.parent.children.add(child);
			 }else{//child.lvl < lvl -> sibling of parent('s parent)
				 CMDElement parent = this.parent;
				 while(child.lvl <= parent.lvl)
					 parent = parent.parent;
				 child.parent = parent;
				 parent.children.add(child);
			 }				 
			return child;
		}
		
		public String toXPath(){			
			String xpath = "";
			while(parent != null){
				xpath = parent.getFullNodeName() + "/" + xpath;
				parent = parent.parent;
			}
			
			return "/" + xpath + (isAttribute? "@" + name : getFullNodeName() + "/text()");
			 
		}
		
		public String getFullNodeName(){
			return NAMESPACE + ":" + name;
		}
		
		//return Map<XPath, Concept>
		public Map<String, String> toMap(){
			Map<String, String> map = new HashMap<>();
			if(!children.isEmpty())
				children.forEach(child -> map.putAll(child.toMap()));
			map.put(toXPath(), concept);
			return map;			
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
}
