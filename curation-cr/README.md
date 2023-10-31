# Curation Component Registry Service (CR)

## What it does
The CR service is managing instances of ProfileCacheEntry. A ProfileCacheEntry is a Java object, which contains all 
xpaths to elements and attributes of implementations of a specific schema (profile), on other words the CMD files 
based on the schema. 
An addition to this the ProfileCacheEntry contains an instance of Schema, which is the Java representation of an XML schema.   

## How it works
A schema (profile) is parsed for element- and attribute-definitions and xpaths are generated from these definitions. 
For those definitions that have a reference to a concept URI, a CRRConcept object is mapped to the xpaths, which allows 
later on to map on xpath to facets (by the mean of facetConcept.xml). 
The creation of the Schema instance is a standard Java procedure. 

## What I should look for
The module makes use of Spring Boot's caching functionality. All classes that contain methods with cached results are in
the cache-package. Whenever you use the module you must make sure to enable caching (@EnableCaching). 