# Concept Registry Service (CCR)

## What it does
The CCR service is managing instances of CCRConcept. A CRRConcept is a Java object which contains information on URI, 
preferred label and status of a specific concept.  

## How it works
The application.yml defines a curation.ccr-service.restApi and a curation.ccr-service.query. Both strings are concatenated
to a URL and a get request is send towards this URL. This request returns a JSON array of JSON objects, which are parsed 
to instances of CCRConcept.  

## What should I look for
The module makes use of Spring Boot's caching functionality. All classes that contain methods with cached results are in
the cache-package. Whenever you use the module you must make sure to enable caching (@EnableCaching). 
