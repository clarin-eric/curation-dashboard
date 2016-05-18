package eu.clarin.cmdi.curation.facets;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class FacetConceptMappingService {

	static final Logger _logger = LoggerFactory.getLogger(FacetConceptMappingService.class);

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

	public int getTotalNumOfFacets() {
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

	public FacetConceptMapping unmarshal(InputStream is) throws Exception {
		XMLMarshaller<FacetConceptMapping> facetConceptMarshaller = new XMLMarshaller<>(FacetConceptMapping.class);
		return facetConceptMarshaller.unmarshal(is);
	}


	private class MappingCreatorThread extends Thread {

		private String profile;

		public MappingCreatorThread(String profile) {
			this.profile = profile;
		}

		public void run() {
			Pair<Profile2FacetMap, Exception> pair = new Pair<>();
			Profile2FacetMap mapping = new Profile2FacetMap();
			try{
				ProfileParser parser = new ProfileParser(profile);
				for(FacetConcept facetConcept: facetConcepts){
					Facet facet = new Facet();
					List<String> xpaths = new ArrayList<>();
					handleId(xpaths, facetConcept);
					//handle concepts
					for(String concept: facetConcept.getConcepts()){
						if(!facetConcept.hasContext())
							xpaths.addAll(parser.getXPathsForConcept(concept));
						else//context exists
							xpaths.addAll(
								parser.getXPathsForConcept(concept)
								.stream()
								.filter(path -> acceptPath(path, getContext(path, parser), facetConcept.getAcceptableContext(),
										facetConcept.getRejectableContext(), facetConcept.getName()))
								.collect(Collectors.toList())
							);
					}
					
					// pattern-based blacklisting: remove all XPath expressions
					// that contain a blacklisted substring;
					// this is basically a hack to enhance the quality of the
					// visualised information in the VLO;
					// should be replaced by a more intelligent approach in the future
					for (String blacklistPattern : facetConcept.getBlacklistPatterns()) {
						Iterator<String> xpathIterator = xpaths.iterator();
						while (xpathIterator.hasNext()) {
							String xpath = xpathIterator.next();
							if (xpath.contains(blacklistPattern)) {
								//_logger.debug("Rejecting {} because of blacklisted substring {}", xpath, blacklistPattern);
								xpathIterator.remove();
							}
						}
					}

					facet.setCaseInsensitive(facetConcept.isCaseInsensitive());
					facet.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
					facet.setName(facetConcept.getName());

					//remove duplicates
					xpaths = xpaths.stream().distinct().collect(Collectors.toList());
					
					facet.setPatterns(xpaths);
					//add patterns if they really exists (if schema supports them)
					facet.setFallbackPatterns(facetConcept.getPatterns().stream().filter(pattern -> parser.hasXPath(pattern)).collect(Collectors.toList()));
					
					facet.setDerivedFacets(facetConcept.getDerivedFacets());

					if (!facet.getPatterns().isEmpty() || !facet.getFallbackPatterns().isEmpty()) {
						mapping.addMapping(facet);
					} else
						mapping.addNotCovered(facet.getName());
				}
				pair.setX(mapping);			
			} catch (Exception e) {
				pair.setY(new Exception("Error creating facetMapping for profile " + profile, e));
			}finally{
				facetMappingCache.putIfAbsent(profile, pair);
				Object lock = beingProcessed.get(profile);
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		}
		
		private void handleId(List<String> xpaths, FacetConcept facetConcept) {
			if ("id".equals(facetConcept.getName())) {
				xpaths.addAll(facetConcept.getPatterns());
			}
		}		
		
		private boolean acceptPath(String path, String context, AcceptableContext acceptableContext, RejectableContext rejectableContext, String facetName){
			// check against acceptable context
			if (acceptableContext != null) {
				if (context == null && acceptableContext.includeEmpty()) {
					// no context is accepted
					_logger.debug("facet[{}] path[{}] context[{}](empty) is accepted", facetName, path, context);
					return true;
				} else if (acceptableContext.getConcepts().contains(context)) {
					// a specific context is accepted
					_logger.debug("facet[{}] path[{}] context[{}] is accepted", facetName, path, context);
					return true;
				}
			}
			// check against rejectable context
			if (rejectableContext != null) {
				if (context == null && rejectableContext.includeEmpty()) {
					// no context is rejected
					_logger.debug("facet[{}] path[{}] context[{}](empty) is rejected", facetName, path, context);
					return false;
				} else if (rejectableContext.getConcepts().contains(context)) {
					// a specific context is rejected
					_logger.debug("facet[{}] path[{}] context[{}] is rejected", facetName, path, context);
					return false;
				} else if (rejectableContext.includeAny()) {
					// any context is rejected
					_logger.debug("facet[{}] path[{}] context[{}](any) is rejected", facetName, path, context);
					return false;
				}
			}
			if (context != null && acceptableContext != null && acceptableContext.includeAny()) {
				// any, not rejected context, is accepted
				_logger.debug("facet[{}] path[{}] context[{}](any) is accepted", facetName, path, context);
				return true;
			}
			
			return false;
		}
		
		/**
		 * Look if there is a contextual (container) data category associated
		 * with an ancestor by walking back.
		 */
		private String getContext(String path, ProfileParser parser) {
			String context = null;
			String cpath = path;
			while (context == null && !cpath.equals("/text()")) {
				cpath = cpath.replaceAll("/[^/]*/text\\(\\)", "/text()");
				context = parser.getConcept(cpath);
			}
			return context;
			
		}
	}
}
