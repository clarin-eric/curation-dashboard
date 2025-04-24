# 7.4.0
- adding timelines for link checking (issue https://github.com/clarin-eric/curation-dashboard/issues/278)
- upgrading to Spring Boot 3.4.4

# 7.3.1
- fix for maven lookup (issue  https://github.com/clarin-eric/curation-dashboard/issues/281)

# 7.3.0
- adding URL parameter »format« in those controllers, where it hasn't existed so far to get the report in JSON, XML or TVS format (issue https://github.com/clarin-eric/curation-dashboard/issues/280)

# 7.2.0
- adding collection history report (issue https://github.com/clarin-eric/curation-dashboard/issues/277)
- renaming collection and file headline in HTML (issue https://github.com/clarin-eric/curation-dashboard/issues/279)
- upgrading to Spring Boot 3.4.2

# 7.1.2
- setting maximum CMDI file size to 100MB (issue https://github.com/clarin-eric/curation-dashboard/issues/275)
- upgrading to Spring Boot 3.4.1

# 7.1.1
- adding tests for curation-web module (issue https://github.com/clarin-eric/curation-dashboard/issues/271)
- bug fixes 
  - ambiguous mapping in linkcheckerCtl (issue https://github.com/clarin-eric/curation-dashboard/issues/273)
  - making accept header optional (issue https://github.com/clarin-eric/curation-dashboard/issues/274)
# 7.1.0
- make HTML reports callable with and without *.html suffix (issue https://github.com/clarin-eric/curation-dashboard/issues/261)
- implementation of persistable cache manager ehcache (issue https://github.com/clarin-eric/curation-dashboard/issues/262)
  - removing file caches (issue https://github.com/clarin-eric/curation-dashboard/issues/267)
- adding columns for public and CR residence in profile overview, profile detail view and collection's profile section (issue https://github.com/clarin-eric/curation-dashboard/issues/264)
- adding score for uniqueness of MD selflinks to collection score (issue https://github.com/clarin-eric/curation-dashboard/issues/265)
- removing link from profile overview to SMC browser (issue https://github.com/clarin-eric/curation-dashboard/issues/266)
- making parallel processing for collection controllable again (issue https://github.com/clarin-eric/curation-dashboard/issues/270)
- shifting scoring for CR residence from CMDI instance to profile (issue https://github.com/clarin-eric/curation-dashboard/issues/272)
- bug fixes
  - generating of non public profile reports used in collections (issue https://github.com/clarin-eric/curation-dashboard/issues/263)
  - assuring correct naming of CMDI instance reports (issue https://github.com/clarin-eric/curation-dashboard/issues/269)
  - replacing remained synchronized access to methods (issue https://github.com/clarin-eric/curation-dashboard/issues/268)

# 7.0.0
- based on Java 21
- using virtual threads in collection processing (https://github.com/clarin-eric/curation-dashboard/issues/228) 
- improving accessibility of web-app (https://github.com/clarin-eric/curation-dashboard/issues/249)
- sending accept header for outgoing http-calls (https://github.com/clarin-eric/curation-dashboard/issues/250)
- processing accept header for incoming calls (https://github.com/clarin-eric/curation-dashboard/issues/251)
- reducing calls to external services in maven tests (https://github.com/clarin-eric/curation-dashboard/issues/244)
- using mock server for service tests (https://github.com/clarin-eric/curation-dashboard/issues/234)
- adding functionality to delete outdated reports (https://github.com/clarin-eric/curation-dashboard/issues/241)
- returning pre-generated profile reports in case of public profile validation (https://github.com/clarin-eric/curation-dashboard/issues/258)
- bug fixes (https://github.com/clarin-eric/curation-dashboard/issues/252, https://github.com/clarin-eric/curation-dashboard/issues/256, https://github.com/clarin-eric/curation-dashboard/issues/257)

# 6.4.1
- fix for rendering issue in tables (issue https://github.com/clarin-eric/curation-dashboard/issues/245)
- adding back to current link in historic reports (issue https://github.com/clarin-eric/curation-dashboard/issues/242)
- re-engineering exception handling to stop thread pool (re-opend issue https://github.com/clarin-eric/curation-dashboard/issues/218)
- adding identification and timeout for schema look-up (re-opened issues https://github.com/clarin-eric/curation-dashboard/issues/222, https://github.com/clarin-eric/curation-dashboard/issues/231)

# 6.4.0
- allowing proxy usage for external HTTP calls (issue https://github.com/clarin-eric/curation-dashboard/issues/227)
- sending User-Agent string with HTTP calls (issue https://github.com/clarin-eric/curation-dashboard/issues/222) 
- setting timeout for connection and reading (issue https://github.com/clarin-eric/curation-dashboard/issues/231)
- making collection report history accessible (issue https://github.com/clarin-eric/curation-dashboard/issues/233)


# 6.3.1
- adapting concept registry access 

# 6.3.0
- upgrade to Spring Boot 3.2.2
- adding origin to duplicated mdSelflinks and rendering list in HTML view (issue #213)
- linking collection to VLO (issue #13)

# 6.2.0
- upgrade to Spring Boot 3.1.4
- adding robots.txt to CurateCtl to block bot access to /download and /record

# 6.1.2
- fix for issue #201
- adding robots.txt in curation-web (CurationCtl)

# 6.1.1
- allowing unzipped download for linkchecker details
- excluding non resolvable URN selflinks from link checking

# 6.1.0
- Refactoring of InstanceParser (issue #186) and InstanceFacetProcessor (issue #184)
- adding facet temporalCoverage in configuration (issue #180)
- adding functionality and view to see the latest link checker status results (issue #97)
- grouping link checking status details by URL (issue #162)
- adding unit tests for profile (issue #166) and CMD record (issue #167)
- dependency upgrades
- bug fixes

# 6.0.2
- bug fix for issue #160 and #162

# 6.0.1
- dependency upgrade of linkchecker-persistence to version 0.0.5 

# 6.0.0
- converting the project to Spring boot project
- using Spring data JPA for persistence (see [linkchecker-persistence](https://github.com/clarin-eric/linkchecker-persistence), Spring caching and processor Beans)
- modularization of the project (app, web, shared api and services)
- reorganization of processors, sub-processors and reports 
- separation of business-logic (in processors and sub-processors) and presentation layer (report instances)   

# 5.4.x
- adding deactivation and deletion of non confirmed links
- externalising help and faq page as markdown

# version 5.3.x
- renaming project to curation-dashboard, modules to curation-core and curation-web
- trimming of URLs for storage and look up
- new category »Invalid_URL« for URLs which can't be processed with Java URL class
- ordering link checking results in reports for severity (from »Ok« to »Invalid_URL«)

# version 5.2.x
- calculate total linkchecker statistics from category statistics and overall from provider group statistics to prevent database access at different times while linkchecker changes data continuously 

# version 5.1.1
- upgrading [RASA](https://github.com/clarin-eric/resource-availability-status-api) dependency to version 4.0.1

# version 5.1.0
- upgrading [RASA](https://github.com/clarin-eric/resource-availability-status-api) dependency to version 4.0.0
- various code changes because of [changes in RASA API](https://github.com/clarin-eric/resource-availability-status-api/blob/master/CHANGES.md)
- reviewing log levels
