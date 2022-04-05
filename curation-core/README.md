# Clarin Curation Module

### Requirements

Java Standard Edition, version 8 or higher
 
### Running curation module
 
 1. `mvn install` in the working directory
 2. `cd curation-core/target`
 
   **Usage:**
```
-c                 curate a collection
-config <file>     a path to the configuration file
-i                 curate an instance
-id <profilesId>   Space separated CLARIN profile IDs in format: clarin.eu:cr1:p_xxx
-p                 curate a profile
-path <path>       Space separated paths to file or folder to be curated
-url <url>         Space separated urls to profile or instance to be curated
```             

 **Configuration**
 
 Configuration is done through a properties file that consists of the following variables. Don't forget to set **OUTPUT_DIRECTORY and CACHE_DIRECTORY**.
 
 ```
  SCORE_NUMERIC_DISPLAY_FORMAT=#0.0000
  TIMESTAMP_DISPLAY_FORMAT=dd-MM-yyyy HH.mm.ss
  
  # files with file-size > MAX_FILE_SIZE won't be analyzed for performance reason
  MAX_FILE_SIZE=10000000
  
  # If SAVE_REPORT=true you must specify an output directory
  SAVE_REPORT=true
  
  # reports are stored in xml-format in this directory
  OUTPUT_DIRECTORY=
  
  # necessary to cache xsd profiles
  CACHE_DIRECTORY=
  
  # facets to analyze (a subset of the facets configured in facetConcepts.xml)s
  FACETS=languageCode, collection, resourceClass, modality, format, keywords, genre, subject, country, organisation, name, description, license, availability
  REDIRECT_FOLLOW_LIMIT=5
  #Timeout for linkchecking in millisecondss
  TIMEOUT=50000

  #database that contains the link checking information
  #database
  DATABASE_USERNAME=
  DATABASE_PASSWORD=
  DATABASE_URI=

 ```

  **Examples:**
  
 - Local instance: `java -jar curation-core-1.2-SNAPSHOT-jar-with-dependencies.jar -config /path/to/clarin-curation-dashboard/curation-core/src/main/resources/config.properties -i -path /path/to/instance/instance.xml`
 - Instance from URL: `java -jar curation-core-1.2-SNAPSHOT-jar-with-dependencies.jar -config /path/to/clarin-curation-dashboard/curation-core/src/main/resources/config.properties -i -url http://url.com/to/instance`
 - Local collection: `java -jar curation-core-1.2-SNAPSHOT-jar-with-dependencies.jar -config /path/to/clarin-curation-dashboard/curation-core/src/main/resources/config.properties -c -path /path/to/collection/`
