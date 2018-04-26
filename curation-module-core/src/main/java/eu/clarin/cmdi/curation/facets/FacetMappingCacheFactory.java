package eu.clarin.cmdi.curation.facets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.facets.FacetConcepts.AcceptableContext;
import eu.clarin.cmdi.curation.facets.FacetConcepts.FacetConcept;
import eu.clarin.cmdi.curation.facets.FacetConcepts.RejectableContext;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap.Facet;

class FacetMappingCacheFactory{
	
	static final long HOUR_IN_NS = 3600000000000L;
	
	private static final Logger logger = LoggerFactory.getLogger(FacetMappingCacheFactory.class);
	
	static LoadingCache<ProfileHeader, Profile2FacetMap> createFacetMappingCache(boolean isPublicProfilesCache){
		return isPublicProfilesCache?
		
		CacheBuilder.newBuilder()
				.concurrencyLevel(4)
				.build(new FacetMappingCacheLoader())
		:
		CacheBuilder.newBuilder()
			.concurrencyLevel(4)
			.expireAfterWrite(8, TimeUnit.HOURS)//keep non public profiles 8 hours in cache
			.ticker(new Ticker() { @Override public long read() { return 9 * HOUR_IN_NS; } }) //cache tick 9 hours
			.build(new FacetMappingCacheLoader());		
	}
	
	/*
	 * This class has a logic to map profile into facet according to facetConcepts.xml
	 */
	
	private static class FacetMappingCacheLoader extends CacheLoader<ProfileHeader, Profile2FacetMap>{		
		
		@Override
		public Profile2FacetMap load(ProfileHeader header) throws Exception{
			logger.info("Mapping for {} is not in the cache, it will be created", header);
			
			Profile2FacetMap mapping = new Profile2FacetMap();
			ParsedProfile parsedProfile = new CRService().getParsedProfile(header);
			for(FacetConcept facetConcept: FacetConceptMappingService.facetConcepts){
				Facet facet = new Facet();
				Collection<String> xpaths = new ArrayList<>(); //<xpath, concept>
				//take all patterns for "id" facet				
				if (FacetConstants.FIELD_ID.equals(facetConcept.getName())) {
					facetConcept.getPatterns().forEach(pattern -> xpaths.add(pattern));
				}
				
				//handle concepts
				for(String concept: facetConcept.getConcepts()){
					if(!facetConcept.hasContext())
						parsedProfile.getXPathsForConcept(concept).forEach(xpath -> xpaths.add(xpath));
					else//context exists
						parsedProfile.getXPathsForConcept(concept)
						.stream()
						.filter(path -> acceptPath(path, getContext(path, parsedProfile), facetConcept.getAcceptableContext(),
								facetConcept.getRejectableContext(), facetConcept.getName()))
						.forEach(xpath -> xpaths.add(xpath));
						
				}
				
				// pattern-based blacklisting: remove all XPath expressions
				// that contain a blacklisted substring;
				// this is basically a hack to enhance the quality of the
				// visualised information in the VLO;
				// should be replaced by a more intelligent approach in the future
				for (String blacklistPattern : facetConcept.getBlacklistPatterns()) {
					//Iterator<String> xpathIterator = xpaths.iterator();
					Iterator<String> xpathIterator = xpaths.iterator();
					while (xpathIterator.hasNext()) {
						String xpath = xpathIterator.next();
						if (xpath.contains(blacklistPattern)) {
							//logger.debug("Rejecting {} because of blacklisted substring {}", xpath, blacklistPattern);
							xpathIterator.remove();
							
							//remove it from map as well
							xpaths.remove(xpath);
						}
					}
				}
				
				facet.setCaseInsensitive(facetConcept.isCaseInsensitive());
				facet.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
				
				facet.setPatterns(xpaths);
				//add patterns if they can be actually evaluated in profile
				facet.setFallbackPatterns(facetConcept.getPatterns().stream().filter(pattern -> parsedProfile.hasXPath(pattern)).collect(Collectors.toList()));
				
				facet.setDerivedFacets(facetConcept.getDerivedFacets());
				
				
				if (!facet.getPatterns().isEmpty() || !facet.getFallbackPatterns().isEmpty()) {
					mapping.addMapping(facetConcept.getName(), facet);
				} else
					mapping.addMapping(facetConcept.getName(), null);
				
			}
			
			return mapping;
		}
		
		private boolean acceptPath(String path, String context, AcceptableContext acceptableContext, RejectableContext rejectableContext, String facetName){
			// check against acceptable context
			if (acceptableContext != null) {
				if (context == null && acceptableContext.includeEmpty()) {
					// no context is accepted
					return true;
				} else if (acceptableContext.getConcepts().contains(context)) {
					// a specific context is accepted
					return true;
				}
			}
			// check against rejectable context
			if (rejectableContext != null) {
				if (context == null && rejectableContext.includeEmpty()) {
					// no context is rejected
					return false;
				} else if (rejectableContext.getConcepts().contains(context)) {
					// a specific context is rejected
					return false;
				} else if (rejectableContext.includeAny()) {
					// any context is rejected
					return false;
				}
			}
			if (context != null && acceptableContext != null && acceptableContext.includeAny()) {
				// any, not rejected context, is accepted
				return true;
			}
			
			return false;
		}
		

		private String getContext(String path, ParsedProfile parsedProfile) {
			String context = null;
			String cpath = path;
			while (context == null && !cpath.equals("/text()")) {
				cpath = cpath.replaceAll("/[^/]*/text\\(\\)", "/text()");
				context = parsedProfile.getConcept(cpath);
			}
			return context;			
		}		
		
	}

}
