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
			int ind = vn.getAttrVal("name");
			String name = "";
			if(ind != -1)
				name = vn.toNormalizedString(ind);
			int lvl = vn.getCurrentDepth();
			
			//System.out.println(name + ", " + lvl);
			if(curr == null)
				root = curr = new CMDElement(name, lvl);
			else{
				curr = curr.addChild(new CMDElement(name, lvl));
				curr.concept = getDatcat(vn);
				vn.push();
				curr.attributes = getAttributes(vn);
				vn.pop();
			}
			
		}
		
		return root;		
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
	
	
	private List<String> getAttributes(VTDNav vn) throws Exception {
		List<String> attributes = new ArrayList<>();		
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./complexType/simpleContent/extension/attribute");
		while(ap.evalXPath() != -1){
			int ind = vn.getAttrVal("name");
			if(ind != -1)
				attributes.add(vn.toNormalizedString(ind));
		}
		
		return attributes;		
	}
	
	
	public static class CMDElement{
		CMDElement parent;
		String name;
		int lvl;
		String concept;
		List<String> attributes;
		
		List<CMDElement> children = null;
		
		public CMDElement(String name, int lvl){
			this.name = name;
			this.lvl = lvl;
			parent = null;
			attributes = null;
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
		
		public List<String> toXPaths(){
			List<String> xpaths = new ArrayList<>();
			String xpath = getFullNodeName();
			while(parent != null){
				xpath = parent.getFullNodeName() + "/" + xpath;
				parent = parent.parent;
			}		
			final String basePath = "/" + xpath;
			
			xpaths.add(basePath + "/text()");
			
			//add attributes
			if(attributes != null)
				attributes.forEach(attr -> xpaths.add(basePath + "/@" + attr));
			
			return xpaths;
			 
		}
		
		public String getFullNodeName(){
			return NAMESPACE + ":" + name;
		}
		
		//return Map<XPath, Concept>
		public Map<String, String> toMap(){
			Map<String, String> map = new HashMap<>();
			if(!children.isEmpty())
				children.forEach(child -> map.putAll(child.toMap()));
			toXPaths().forEach(xpath -> map.put(xpath, concept));
			return map;			
		}
	}
	
}
