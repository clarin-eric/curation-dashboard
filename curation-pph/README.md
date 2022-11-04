# Curation Public Profile Header Service (PPH)

## What it does
The CCR service is providing a Map of ProfileHeader. A Profile is a Java object which contains profile informations id, name, description, etc. 
The key of each ProfileHeader is its id.  

## How it works
The application.yml defines a curation.pph-service.restApi and a curation.pph-service.query. Both is combined to an URL 
and a get request the this URL. This request returns an XML document of profileDescription elements for public profiles. 
Each element is parsed to a ProfileHeader object. 

## What should I look for
The Map of ProfileHeader is declared as Cacheable to have the opportunity to configure a reload of the Map with the means of 
Spring Boot. Whenever you call the Map you MUST make sure to enable caching (@EnableCaching) since otherwise the Map is 
reloaded for each call.   
