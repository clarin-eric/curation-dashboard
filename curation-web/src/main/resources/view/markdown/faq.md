# Frequently Asked Questions

1. **What is the Curation Dashboard?**

    Curation Dashboard is a software bundle, which determines and grades
    the quality of metadata harvested by CLARIN for language resources, in order to support their authors and
    curators to improve the provided metadata quality. 
    It consists of three parts: a stand alone application for reports generation (curation-core), a web application (curation-web) 
    and another stand alone application for link checking (linkchecker). 

1. **What do VLO, record, collection, CMDI and other terms mean?**

    For a general overview of VLO, you can visit its [about page](https://vlo.clarin.eu/about). 
    For more general info, [the CLARIN Metadata FAQ page](https://vlo.clarin.eu/about) is a good starting point.

1. **What does the Curation Dashboard do exactly?**

    The Curation Dashboard processes publicly available CMDI records, 
    collections and profiles. It grades them and gives them scores
    based on different quality aspects. It also provides information on 
    them and on what properties were graded.
         
1. **What is a 'collection' and where does the data come from?**
    
     A collection contains the metadata harvested by the VLO harvester
     from a single 'endpoint', i.e. all the metadata from one repository and/or
     CLARIN center.
     This metadata is the output of the harvesting process, the result of which 
     can be found at https://vlo.clarin.eu/.
     
1. **How often does the Curation Dashboard generate reports?**

    The core-application of Curation Dashboard generates the reports three times a week in the early morning hours CET.
    
1. **Is there a limit on file sizes?**

    Yes, the Curation Dashboard doesn't process files larger than 50 megabytes. 
    Such files are ignored when collection reports are generated.
        
1. **What is the Link Checker?**

    The Link Checker is a stand alone application which checks the availability of a resource at the addresses
    referenced in the metadata. In practice, the resources are URLs (or more commonly links),
    which can be checked via HTTP requests. The Link Checker then saves the responses to the requests in a database. 
    The links are extracted from CMD Records within the collections.
    Results of the checking can be directly viewed on the [Link Checker Statistics page](https://curation.clarin.eu/statistics)
    and they also affect the overall score of the collections. 
    
1. **How does scoring work?**
     
    A score value is calculated for profiles, instances and collections (which is the sum of its instance scores). 
    The next two tables show the criteria on which the scoring is based on as well as the value set:

    **Profile**
    
    |Context|Criteria|Value set|
    |---|---|---|
    |Header|Profile is public?|{0, 1}|
    |Facet|Percentage of defined facets covered by profile|[0, 1]|
    |Cmd-concepts|Percentage of elements (except header and resources) with concept|[0, 1]|
    ||**Sum**|[0, 3]|
    
    **Instance**
    
    |Context|Criteria|Value set|
    |---|---|---|
    |File size|File size <= defined file size?|{0, 1}
    ||**Sum File size**|{0, 1}|
    |Header|Valid schema location from attribute “schemaLocation” or “noNamespaceSchemaLocation” available?|{0, 1}|
    |Header|Schema comes from Component Registry?|{0, 1}|
    |Header|MdProfile available and valid (against regular expression)?|{0, 1}|
    |Header|MdCollectionDisplayName available?|{0, 1}|
    |Header|MdSelfLink available?|{0, 1}|
    ||**Sum Header**|{1,..., 5}|
    |Facet|Percentage of of defined facets covered by instance|[0, 1]|
    ||**Sum Facet**|[0, 1]|
    |URL|Percentage of valid links|[0, 1]|
    ||**Sum URL**|[0, 1]|
    |XML|Is the xml valid?|{0, 1}|
    |XML|Percentage of populated elements|[0, 1]|
    ||**Sum XML**|[0, 2]|
    ||**Sum Profile**|[0, 3]|
    |Resource Proxy|Percentage of RP with mime type|[0, 1]|
    |Resource Proxy|Percentage of RP with references|[0, 1]|
    ||**Sum Resource Proxy**|[0, 2]|
    ||**Sum over all**|[0, 15]|
          
    
1. **What technology is the Link Checker based?**

    The old implementation of the Link Checker was replaced by a new 
    [codebase](https://github.com/clarin-eric/linkchecker), 
    which is based on [Stormcrawler](http://stormcrawler.net/), 
    which in turn is based on [Apache Storm](https://storm.apache.org/).

1. **How does the Link Checker work?**

    When the Curation Dashboard generates its collection reports, all resource links
    within the records are extracted and saved into
    a database. The Link Checker then continuously checks these links and saves their results in the database. 
    At the time of writing, there
    are approximately 7 million links, so the Link Checker never stops. It takes approximately two months to go over all the links.
    When it's finished, it restarts, so that the results can stay up-to-date. Note
    that as a result of the prioritisation logic inside the Link Checker,
    it doesn't necessarily take two months before the status of a given link is updated.
    
1. **What request method does the Link Checker use?**

    The Link Checker always sends a `HEAD` request first. If it is unsuccessful for whatever reason, it tries
    a `GET` request. However, it doesn't read the response payload in case of `GET`. All info is extracted from
    the status code and the headers.
    
1. **Does Link Checker follow redirects?**

    Yes it does. It even records how many redirects the link points to. 
    However, there is a hard limit of 20 redirects.
    
1. **What is 0 status code?**

    Originally, we decided to regard any link, of which we can't get
    a response to have the 0 status code. This bundled up links with 
    many different problems into one and is not very clear. Therefore, 
    we plan to get rid of the 0 status code. We will change the classification
    system from status codes into categories to make the results more transparent.

1. **What categories are there?**

    Currently there are only 5 categories: 
    
    1. Ok
    2. Broken
    3. Undetermined
    4. Restricted access
    5. Blocked by robots.txt
    
    They are color coded as green, red, yellow, blue and purple respectively. 
    
1. **The Curation Dashboard reports my links incorrectly. What should I do?**

    If you suspect the reason for the reports being wrong is the Link Checker and your links work fine,
    please create an issue on our [github page](https://github.com/clarin-eric/curation-dashboard/issues).     
    
1. **The byte size of my link is shown as null but the link has a correct response body. What's wrong?**

    The HTTP Header `Content-Length` is taken as the single source of truth for the byte size. `HEAD`
    requests don't contain a response payload by definition and Link Checker doesn't read the payload of
    `GET` requests to save bandwidth and time. Therefore the only reliable source is the `Content-Length` header.
    Please set it correctly wherever you can on your servers.
    
1. **What changes and new features are yet to be implemented?**

    1. Change classification system from status codes into categories
    2. Show historical results for the links (a list of every check of a particular link)
    3. A csv file containing all the links in a particular link checker statistics view (it currently loads only 100 links)
    4. A fast lane for links to check them as soon as possible (to avoid waiting 2 months to check a link again)
    
    If you wish to suggest a change or a new feature, feel free to do so on our [github page](https://github.com/clarin-eric/curation-dashboard/issues).
    
1. **The Link Checker is making more requests than my server can handle, or
     even causing high loads on my servers. What should I do?**

    The Link Checker respects `robots.txt` if the server provides it. If the server doesn't provide
    any crawl delay information on its `robots.txt`, it will send requests as fast as it can 
    but never more than one at a time. We absolutely want to avoid causing high loads
    on your servers, so please [get in touch](mailto:linkchecker@clarin.eu), so that we can fix the issue.  
    
1. **Where does "Expected Content Type" come from?**

    It is extracted from CMD Records. It is however not specified for all links.
    
1. **I have more questions. Where can I ask them?**

    Feel free to ask any questions by creating issues on our [github page](https://github.com/clarin-eric/curation-dashboard/issues).
    
1. **Where can I report issues?**

    Feel free to report issues on our [github page](https://github.com/clarin-eric/curation-dashboard/issues).
