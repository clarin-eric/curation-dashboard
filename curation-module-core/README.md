# Clarin Curation Module
 
 The goal of this project is to implement software component for curation and quality assessment which can be integrated in the CLARINs VLO workflow. Project is initialized by Metadata Curation Task Force. Specification for the Curation Module is based on the Metadata Quality Assessement Service proposal. Curation Module validates and normalizes single MD records, repositories and profiles, to assess their quality and to produce reports with different information for different actors in VLO workflow. For implementation this project will use some of the existing CLARIN components. 
 
 ### Running curation module
 
 1. `mvn install` in the working directory
 2. `cd curation-module-core/target`
 3. `java -jar curation-module-core-1.2-SNAPSHOT-jar-with-dependencies.jar [flags]`
 
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
  MAX_FILE_SIZE=10000000
  HTTP_VALIDATION=false
  SAVE_REPORT=true
  OUTPUT_DIRECTORY=
  CACHE_DIRECTORY=
  FACETS=languageCode, collection, resourceClass, modality, format, keywords, genre, subject, country, organisation,  nationalProject, name, description, license, availability
  REDIRECT_FOLLOW_LIMIT=5
  #Timeout in ms
  TIMEOUT=5000
 ```

  **Examples:**
  
 - Local instance: `java -jar curation-module-core-1.2-SNAPSHOT-jar-with-dependencies.jar -config /path/to/clarin-curation-module/curation-module-core/src/main/resources/config.properties -i -path /path/to/instance/instance.xml`
 - Instance from URL: `java -jar curation-module-core-1.2-SNAPSHOT-jar-with-dependencies.jar -config /path/to/clarin-curation-module/curation-module-core/src/main/resources/config.properties -i -url http://url.com/to/instance`
 - Local collection: `java -jar curation-module-core-1.2-SNAPSHOT-jar-with-dependencies.jar -config /path/to/clarin-curation-module/curation-module-core/src/main/resources/config.properties -c -path /path/to/collection/`
