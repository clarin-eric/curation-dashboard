# 6.4.1
- fix for rendering issue in tables (issue #245)
- adding back to current link in historic reports (issue #242)
- re-engineering exception handling to stop thread pool (re-opend issue #218)
- adding identification and timeout for schema look-up (re-opened issues #222, #231)

# 6.4.0
- allowing proxy usage for external HTTP calls (issue #227)
- sending User-Agent string with HTTP calls (issue #222) 
- setting timeout for connection and reading (issue #231)
- making collection report history accessible (issue #233)


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