# Curation Public Profile Header Service (PPH)

## What it does
The PPH service os managing instances of ProfileHeader. A ProfileHeader is a Java object which contains information 
on id, name, description, etc. of a specific profile. 

## How it works
The application.yml defines a curation.pph-service.restApi and a curation.pph-service.query. Both strings are concatenated 
to a URL and a get request is send towards this URL. This request returns an XML document of profileDescription elements,  
which are parsed to instances of ProfileHeader. 

## What should I look for
The module makes use of Spring Boot's caching functionality. All classes that contain methods with cached results are in 
the cache-package. Whenever you use the module you must make sure to enable caching (@EnableCaching). 
