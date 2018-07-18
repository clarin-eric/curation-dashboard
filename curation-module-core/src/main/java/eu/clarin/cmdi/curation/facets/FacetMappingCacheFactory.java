package eu.clarin.cmdi.curation.facets;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.*;

public class FacetMappingCacheFactory extends FacetMappingFactory{
    private final LoadingCache<ProfileHeader, FacetMapping> facetMappingPublicCache;
    private final LoadingCache<ProfileHeader, FacetMapping> facetMappingNonPublicCache;
    
    private static FacetMappingCacheFactory _facetMappingCacheFactory;
    
    public static FacetMappingCacheFactory getInstance() throws IOException {
        if(_facetMappingCacheFactory == null) {
            VloConfig vloConfig = new DefaultVloConfigFactory().newConfig();
            VLOMarshaller marshaller = new VLOMarshaller();
            _facetMappingCacheFactory = new FacetMappingCacheFactory(vloConfig,marshaller);
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

    
    private class FacetMappingCacheLoader extends CacheLoader<ProfileHeader, FacetMapping>{
        @Override
        public FacetMapping load(ProfileHeader header) throws Exception{
            return FacetMappingCacheFactory.this.createMapping(header.id, false);
            
        }
    }
    
    public Collection<String> getFacetNames() {
        return super.conceptMapping.getFacetConcepts().stream().map(FacetConceptMapping.FacetConcept::getName).collect(Collectors.toList());
    }
}
