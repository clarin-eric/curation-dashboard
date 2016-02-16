package eu.clarin.cmdi.curation.facets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.component_registry.ComponentRegistryService;
import eu.clarin.cmdi.curation.facets.FacetConceptMapping.AcceptableContext;
import eu.clarin.cmdi.curation.facets.FacetConceptMapping.FacetConcept;
import eu.clarin.cmdi.curation.facets.FacetConceptMapping.RejectableContext;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap.Facet;
import eu.clarin.cmdi.curation.utils.Pair;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;

/**
 * @author dostojic
 *
 */
public class FacetConceptMappingService implements XMLMarshaller<FacetConceptMapping> {

    static final Logger _logger = LoggerFactory.getLogger(FacetConceptMapping.class);

    // singleton
    private static FacetConceptMappingService instance = null;

    // move this to config
    public static final String FACET_CONCEPTS_URL = "https://raw.githubusercontent.com/clarin-eric/VLO/master/vlo-commons/src/main/resources/facetConcepts.xml";

    private Collection<FacetConcept> facetConcepts;

    private final Collection<String> facetConceptsNames;
    private final int totalNumOfFacets;

    private final Map<String, Pair<Profile2FacetMap, Exception>> facetMappingCache = new ConcurrentHashMap<>();
    private final Map<String, Object> beingProcessed = new ConcurrentHashMap<>();

    public synchronized static FacetConceptMappingService getInstance() throws Exception {
	if (instance == null)
	    instance = new FacetConceptMappingService();
	return instance;

    }

    private FacetConceptMappingService() throws Exception {
	facetConcepts = createFacetConcept();
	facetConceptsNames = facetConcepts.stream().map(FacetConcept::getName).collect(Collectors.toList());
	totalNumOfFacets = facetConceptsNames.size();
    }

    public Collection<String> getFacetConceptsNames() {
	return facetConceptsNames;
    }
    
    public int getTotalNumOfFacets(){
	return totalNumOfFacets;
    }

    public Profile2FacetMap getMapping(String profile) throws Exception {
	if (!facetMappingCache.containsKey(profile)) {
	    // if null means that key didn't existed
	    if (beingProcessed.putIfAbsent(profile, new Object()) == null) {
		new MappingCreatorThread(profile).start();
	    }
	}

	while (!facetMappingCache.containsKey(profile)) {
	    Object lock = beingProcessed.get(profile);
	    synchronized (lock) {
		lock.wait();
	    }
	}

	Pair<Profile2FacetMap, Exception> mapping = facetMappingCache.get(profile);
	if (mapping.getY() != null)
	    throw mapping.getY();

	return mapping.getX();

    }

    private Collection<FacetConcept> createFacetConcept() throws MalformedURLException, IOException, Exception {
	return unmarshal(new URL(FACET_CONCEPTS_URL).openStream()).facetConcepts;
    }

    @Override
    public FacetConceptMapping unmarshal(InputStream is) throws Exception {
	JAXBContext jc = JAXBContext.newInstance(FacetConceptMapping.class);
	Unmarshaller unmarshaller = jc.createUnmarshaller();
	return (FacetConceptMapping) unmarshaller.unmarshal(is);
    }

    @Override
    public void marshal(FacetConceptMapping object, OutputStream os) throws Exception {
	throw new UnsupportedOperationException();
    }

    private class MappingCreatorThread extends Thread {

	private String profile;

	public MappingCreatorThread(String profile) {
	    this.profile = profile;
	}

	public void run() {
	    Pair<Profile2FacetMap, Exception> pair = new Pair<>();
	    Profile2FacetMap mapping = new Profile2FacetMap();
	    try {
		// The magic
		Map<String, List<String>> conceptLinkPathMapping = createConceptLinkPathMapping();
		Map<String, String> pathConceptLinkMapping = null;
		// Below we put the stuff we found into the configuration class.
		for (FacetConcept facetConcept : facetConcepts) {
		    Facet config = new Facet();
		    List<String> xpaths = new ArrayList<String>();
		    handleId(xpaths, facetConcept);
		    for (String concept : facetConcept.getConcepts()) {
			List<String> paths = conceptLinkPathMapping.get(concept);
			if (paths != null) {
			    if (facetConcept.hasContext()) {
				for (String path : paths) {
				    // lazily instantiate the reverse mapping,
				    // i.e.,
				    // from concept to path
				    if (pathConceptLinkMapping == null) {
					pathConceptLinkMapping = new HashMap<String, String>();
					for (String c : conceptLinkPathMapping.keySet()) {
					    for (String p : conceptLinkPathMapping.get(c)) {
						pathConceptLinkMapping.put(p, c);
					    }
					}
				    }
				    String context = getContext(path, pathConceptLinkMapping);
				    boolean handled = false;
				    // check against acceptable context
				    if (facetConcept.hasAcceptableContext()) {
					AcceptableContext acceptableContext = facetConcept.getAcceptableContext();
					if (context == null && acceptableContext.includeEmpty()) {
					    // no context is accepted
					    _logger.debug("facet[{}] path[{}] context[{}](empty) is accepted",
						    facetConcept.getName(), path, context);
					    xpaths.add(path);
					    handled = true;
					} else if (acceptableContext.getConcepts().contains(context)) {
					    // a specific context is accepted
					    _logger.debug("facet[{}] path[{}] context[{}] is accepted",
						    facetConcept.getName(), path, context);
					    xpaths.add(path);
					    handled = true;
					}
				    }
				    // check against rejectable context
				    if (!handled && facetConcept.hasRejectableContext()) {
					RejectableContext rejectableContext = facetConcept.getRejectableContext();
					if (context == null && rejectableContext.includeEmpty()) {
					    // no context is rejected
					    _logger.debug("facet[{}] path[{}] context[{}](empty) is rejected",
						    facetConcept.getName(), path, context);
					    handled = true;
					} else if (rejectableContext.getConcepts().contains(context)) {
					    // a specific context is rejected
					    _logger.debug("facet[{}] path[{}] context[{}] is rejected",
						    facetConcept.getName(), path, context);
					    handled = true;
					} else if (rejectableContext.includeAny()) {
					    // any context is rejected
					    _logger.debug("facet[{}] path[{}] context[{}](any) is rejected",
						    facetConcept.getName(), path, context);
					    handled = true;
					}
				    }
				    if (!handled && context != null && facetConcept.hasAcceptableContext()
					    && facetConcept.getAcceptableContext().includeAny()) {
					// any, not rejected context, is
					// accepted
					_logger.debug("facet[{}] path[{}] context[{}](any) is accepted",
						facetConcept.getName(), path, context);
					xpaths.add(path);
				    }
				}
			    } else {
				xpaths.addAll(paths);
			    }
			}
		    }

		    // pattern-based blacklisting: remove all XPath expressions
		    // that
		    // contain a blacklisted substring;
		    // this is basically a hack to enhance the quality of the
		    // visualised information in the VLO;
		    // should be replaced by a more intelligent approach in the
		    // future
		    for (String blacklistPattern : facetConcept.getBlacklistPatterns()) {
			Iterator<String> xpathIterator = xpaths.iterator();
			while (xpathIterator.hasNext()) {
			    String xpath = xpathIterator.next();
			    if (xpath.contains(blacklistPattern)) {
				_logger.debug("Rejecting {} because of blacklisted substring {}", xpath,
					blacklistPattern);
				xpathIterator.remove();
			    }
			}
		    }

		    config.setCaseInsensitive(facetConcept.isCaseInsensitive());
		    config.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
		    config.setName(facetConcept.getName());

		    LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>(xpaths);
		    if (xpaths.size() != linkedHashSet.size()) {
			_logger.error("Duplicate XPaths in : " + xpaths);
		    }
		    config.setPatterns(new ArrayList<String>(linkedHashSet));
		    config.setFallbackPatterns(facetConcept.getPatterns());
		    config.setDerivedFacets(facetConcept.getDerivedFacets());

		    if (!config.getPatterns().isEmpty() || !config.getFallbackPatterns().isEmpty()) {
			mapping.addMapping(config);
		    }else
			mapping.addNotCovered(config.getName());
		}
		pair.setX(mapping);
	    } catch (Exception e) {
		pair.setY(new Exception("Error creating facetMapping for profile " + profile, e));
	    }

	    facetMappingCache.putIfAbsent(profile, pair);
	    Object lock = beingProcessed.get(profile);
	    synchronized (lock) {
		lock.notifyAll();
	    }
	}

	/**
	 * Look if there is a contextual (container) data category associated
	 * with an ancestor by walking back.
	 */
	private String getContext(String path, Map<String, String> pathConceptLinkMapping) {
	    String context = null;
	    String cpath = path;
	    while (context == null && !cpath.equals("/text()")) {
		cpath = cpath.replaceAll("/[^/]*/text\\(\\)", "/text()");
		context = pathConceptLinkMapping.get(cpath);
	    }
	    return context;
	}

	/**
	 * The id facet is special case and patterns must be added first. The
	 * standard pattern to get the id out of the header is the most reliable
	 * and it should fall back on concept matching if nothing matches. (Note
	 * this is the exact opposite of other facets where the concept match is
	 * probably better then the 'hardcoded' pattern).
	 */
	private void handleId(List<String> xpaths, FacetConcept facetConcept) {
	    if ("id".equals(facetConcept.getName())) {
		xpaths.addAll(facetConcept.getPatterns());
	    }
	}

	/**
	 * "this is where the magic happens". Finds paths in the xsd to all
	 * concepts (isocat data catagories).
	 *
	 * @param xsd
	 *            URL of XML Schema of some CMDI profile
	 * @param useLocalXSDCache
	 *            use local XML schema files instead of accessing the
	 *            component registry
	 * @return Map (Data Category -> List of XPath expressions linked to the
	 *         key data category which can be found in CMDI files with this
	 *         schema)
	 * @throws NavException
	 */
	private Map<String, List<String>> createConceptLinkPathMapping() throws Exception {
	    Map<String, List<String>> result = new HashMap<String, List<String>>();
	    VTDGen vg = new VTDGen();
	    boolean parseSuccess;
	    parseSuccess = vg.parseFile(ComponentRegistryService.getInstance().getSchemaFile(profile).getAbsolutePath(),
		    true);

	    if (!parseSuccess) {
		_logger.error("Cannot create ConceptLink Map from xsd (xsd is probably not reachable): " + profile
			+ ". All metadata instances that use this xsd will not be imported correctly.");
		return result; // return empty map, so the incorrect xsd is not
			       // tried for all metadata instances that specify
			       // it.
	    }
	    VTDNav vn = vg.getNav();
	    AutoPilot ap = new AutoPilot(vn);
	    ap.selectElement("xs:element");
	    Deque<Token> elementPath = new LinkedList<Token>();
	    while (ap.iterate()) {
		int i = vn.getAttrVal("name");
		if (i != -1) {
		    String elementName = vn.toNormalizedString(i);
		    updateElementPath(vn, elementPath, elementName);
		    int datcatIndex = getDatcatIndex(vn);
		    if (datcatIndex != -1) {
			String conceptLink = vn.toNormalizedString(datcatIndex);
			String xpath = createXpath(elementPath);
			List<String> values = result.get(conceptLink);
			if (values == null) {
			    values = new ArrayList<String>();
			    result.put(conceptLink, values);
			}
			values.add(xpath);
		    }
		}
	    }
	    return result;
	}

	/**
	 * Goal is to get the "datcat" attribute. Tries a number of different
	 * favors that were found in the xsd's.
	 *
	 * @return -1 if index is not found.
	 */
	private int getDatcatIndex(VTDNav vn) throws NavException {
	    int result = -1;
	    result = vn.getAttrValNS("http://www.isocat.org/ns/dcr", "datcat");
	    if (result == -1) {
		result = vn.getAttrValNS("http://www.isocat.org", "datcat");
	    }
	    if (result == -1) {
		result = vn.getAttrVal("dcr:datcat");
	    }
	    return result;
	}

	/**
	 * Given an xml-token path thingy create an xpath.
	 *
	 * @param elementPath
	 * @return
	 */
	private String createXpath(Deque<Token> elementPath) {
	    StringBuilder xpath = new StringBuilder("/");
	    for (Token token : elementPath) {
		xpath.append("c:").append(token.name).append("/");
	    }
	    return xpath.append("text()").toString();
	}

	/**
	 * does some updating after a step. To keep the path proper and path-y.
	 *
	 * @param vn
	 * @param elementPath
	 * @param elementName
	 */
	private void updateElementPath(VTDNav vn, Deque<Token> elementPath, String elementName) {
	    int previousDepth = elementPath.isEmpty() ? -1 : elementPath.peekLast().depth;
	    int currentDepth = vn.getCurrentDepth();
	    if (currentDepth == previousDepth) {
		elementPath.removeLast();
	    } else if (currentDepth < previousDepth) {
		while (currentDepth <= previousDepth) {
		    elementPath.removeLast();
		    previousDepth = elementPath.peekLast().depth;
		}
	    }
	    elementPath.offerLast(new Token(currentDepth, elementName));
	}

	private class Token {

	    final String name;
	    final int depth;

	    public Token(int depth, String name) {
		this.depth = depth;
		this.name = name;
	    }

	    @Override
	    public String toString() {
		return name + ":" + depth;
	    }
	}
    }

    public static void test1() {
	FacetConceptMappingService fcmSrv;
	try {
	    fcmSrv = FacetConceptMappingService.getInstance();
	} catch (Exception e) {
	    throw new RuntimeException("Unable to create FacetConceptMapping", e);
	}

	Collection<String> profiles = new LinkedList();
	profiles.add("p_1357720977520");
	profiles.add("p_1297242111880");
	profiles.add("p_1361876010587");
	profiles.add("p_1361876010587");
	profiles.add("p_1357720977520");
	profiles.add("p_1297242111880");
	profiles.add("p_1357720977520");
	profiles.add("p_1361876010587");
	profiles.add("p_1297242111880");

	profiles.parallelStream().forEach(profile -> {
	    try {
		Profile2FacetMap map = fcmSrv.getMapping(profile);

		List<String> uncoveredFacets = new LinkedList<>();
		for (String facet : fcmSrv.getFacetConceptsNames())
		    if (!map.getFacetNames().contains(facet))
			uncoveredFacets.add(facet);

		_logger.debug("Mapping successfully generated for {}, uncovered facets: {}", profile,
			String.join(", ", uncoveredFacets));

	    } catch (Exception e) {
		_logger.error("", e);
	    }
	});
    }

    public static void main(String[] args) {
	test1();
    }

}
