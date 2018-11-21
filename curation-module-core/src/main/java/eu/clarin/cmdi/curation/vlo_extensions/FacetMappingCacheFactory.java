package eu.clarin.cmdi.curation.vlo_extensions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ximpleware.NavException;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.*;

public class FacetMappingCacheFactory extends FacetMappingFactory{
    private final LoadingCache<ProfileHeader, FacetMapping> facetMappingPublicCache;
    private final LoadingCache<ProfileHeader, FacetMapping> facetMappingNonPublicCache;
    
    private static FacetMappingCacheFactory _facetMappingCacheFactory;
    
    public static FacetMappingCacheFactory getInstance() throws IOException {
        if(_facetMappingCacheFactory == null) {
            VLOMarshaller marshaller = new VLOMarshaller();
            _facetMappingCacheFactory = new FacetMappingCacheFactory(Configuration.VLO_CONFIG ,marshaller);
        }
        return _facetMappingCacheFactory;
    }
    

    private FacetMappingCacheFactory(VloConfig vloConfig, VLOMarshaller marshaller) {
        super(vloConfig, marshaller);
        // TODO Auto-generated constructor stub
        
        this.facetMappingPublicCache = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .build(new FacetMappingCacheLoader());
        
        this.facetMappingNonPublicCache = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .expireAfterWrite(8, TimeUnit.HOURS)//keep non public profiles 8 hours in cache
                .build(new FacetMappingCacheLoader());     
    }
    
    public FacetMapping getFacetMapping(ProfileHeader header) throws ExecutionException {
        return header.isPublic?this.facetMappingPublicCache.get(header):this.facetMappingNonPublicCache.get(header);
    }
    
    public Map<String, List<Pattern>> createConceptLinkPathMapping(ProfileHeader header) throws Exception{
        Map<String, List<Pattern>> result = new HashMap<>();
        
        Map<String, CMDINode> elements = new CRService().getParsedProfile(header).getElements();
        
        for(Map.Entry<String, CMDINode> element : elements.entrySet()) {
            result.computeIfAbsent(element.getValue().concept.uri, k -> new ArrayList<Pattern>()).add(new Pattern(element.getKey()));
        }
        
        return result;
    }

    
    private class FacetMappingCacheLoader extends CacheLoader<ProfileHeader, FacetMapping>{
        @Override
        public FacetMapping load(ProfileHeader header) throws Exception{
            return FacetMappingCacheFactory.this.createMapping(
                  new ConceptLinkPathMapper() {

                    @Override
                    public Map<String, List<Pattern>> createConceptLinkPathMapping()
                            throws NavException, URISyntaxException {
                        Map<String, List<Pattern>> result = new HashMap<>();
                        
                        Map<String, CMDINode> elements;
                        try {
                            elements = new CRService().getParsedProfile(header).getElements();
                        }
                        catch (Exception ex) {
                            throw new NavException();
                        }
                        
                        for(Map.Entry<String, CMDINode> element : elements.entrySet()) {
                            if(element.getValue().concept != null)
                                result.computeIfAbsent(element.getValue().concept.uri, k -> new ArrayList<Pattern>()).add(new Pattern(element.getKey()));
                        }
                        
                        return result;
                    }

                    @Override
                    public String getXsd() {
                        return header.id;
                    }

                    @Override
                    public Boolean useLocalXSDCache() {
                        return true;
                    }
                      
                  }
            );      
        }
    }
}
