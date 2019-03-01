# Clarin Curation Module

### Requirements

Java Standard Edition, version 8 or higher
 
### Running curation module
 
 1. `mvn install` in the working directory
 2. `cd curation-module-core/target`
 3. java 8`java -jar curation-module-core-1.2-SNAPSHOT-jar-with-dependencies.jar [flags]`
    java 9/10`java --add-modules java.xml.bind -jar curation-module-core-1.2-SNAPSHOT-jar-with-dependencies.jar [flags]`
 
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
  
  # validation of all http-links of the analyzed CMDI file
  # if HTTP_VALIDATION=true you should use a database and the linkChecker to externalize link-ckecking
  HTTP_VALIDATION=false
  
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
  # MonoDB database configuration (recommeded if HTTP_VALIDATION is true!!!)
  DATABASE=true
  DATABASE_NAME=dbnamee

  # if this is empty, it will try localhost:27017
  DATABASE_URI=mongodb://<username:password>@<MongoDB server>:<port>
 ```

  **Examples:**
  
 - Local instance: `java -jar curation-module-core-1.2-SNAPSHOT-jar-with-dependencies.jar -config /path/to/clarin-curation-module/curation-module-core/src/main/resources/config.properties -i -path /path/to/instance/instance.xml`
 - Instance from URL: `java -jar curation-module-core-1.2-SNAPSHOT-jar-with-dependencies.jar -config /path/to/clarin-curation-module/curation-module-core/src/main/resources/config.properties -i -url http://url.com/to/instance`
 - Local collection: `java -jar curation-module-core-1.2-SNAPSHOT-jar-with-dependencies.jar -config /path/to/clarin-curation-module/curation-module-core/src/main/resources/config.properties -c -path /path/to/collection/`
