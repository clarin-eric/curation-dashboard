# version 5.3.0
- renaming project to curation-dashboard, modules to curation-core and curation-web
- trimming of URLs for storage and look up
- new category »Invalid_URL« for URLs which can't be processed with Java URL class
- ordering link checking results in reports for severity (from »Ok« to »Invalid_URL«)

# version 5.2.0
- calculate total linkchecker statistics from category statistics and overall from provider group statistics to prevent database access at different times while linkchecker changes data continuously 

# version 5.1.1
- upgrading [RASA](https://github.com/clarin-eric/resource-availability-status-api) dependency to version 4.0.1

# version 5.1.0
- upgrading [RASA](https://github.com/clarin-eric/resource-availability-status-api) dependency to version 4.0.0
- various code changes because of [changes in RASA API](https://github.com/clarin-eric/resource-availability-status-api/blob/master/CHANGES.md)
- reviewing log levels