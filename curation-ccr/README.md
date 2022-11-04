# Concept Registry Service (CCR)

## What it does
The CCR service is providing a Map of CCRConcept. A CRRConcept is a Java object which contains the concept informations URI, 
preferred label and status. The key of each CCRConcept is its URI.  

## How it works
The application.yml defines a curation.ccr-service.restApi and a curation.ccr-service.query. Both is combined to an URL 
and a get request the this URL. This request returns a JSON array of JSON objects, where each JSON object contains the required 
concept information. The array is parsed and each concept information stored in a CCRConcept object.  

## What should I look for
The Map of CCRConcept is declared as Cacheable to have the opportunity to configure a reload of the Map with the means of 
Spring Boot. Whenever you call the Map you MUST make sure to enable caching (@EnableCaching) since otherwise the Map is 
reloaded for each call.   
