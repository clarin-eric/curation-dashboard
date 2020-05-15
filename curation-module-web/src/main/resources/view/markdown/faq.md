# Frequently Asked Questions

1. **What is Curation Module?**

    Curation Module is a web application, which determines and grades
    the quality of Clarin Metadata for language resources, in order to support their authors and
    curators to improve their quality.

1. **What do VLO, record, collection, CMDI and other terms mean?**

    For a general overview of VLO, you can visit its [about page](https://vlo.clarin.eu/about). 
    For more general info, [Clarin Metadata FAQ page](https://vlo.clarin.eu/about) is a good starting point.

1. **What does Curation Module do exactly?**

    Curation Module processes publicly availble CMD records, 
    collections and profiles. It grades them and gives them scores
    based on different quality aspects. It also provides information on 
    them and on what properties were graded.
         
1. **Where do the collection data come from?**
    
     The metadata comes from the VLO Harvester and the results of the harvesting process 
     can be found at https://vlo.clarin.eu/.
     
1. **How often does Curation Module generate reports?**

    It generates the reports twice weekly: tuesday and saturday nights CET.
    
1. **Is there a limit on file sizes?**

    Yes, curation module doesn't process files larger than 50 mb. 
    These files are ignored when collection reports are generated.
        
1. **What is Link Checker?**

    Link Checker checks links and saves their results in a database. 
    The links are extracted from CMD Records within the collections.
    Results of the checking can be directly viewed on the [Link Checker Statistics page](https://curate.acdh.oeaw.ac.at/statistics)
    and they also affect the overall score of the collections.
    
1. **What is Stormychecker?**

    Stormychecker is the new name for Linkchecker. The old implementation was replaced by a new 
    codebase, which is based on [Stormcrawler](http://stormcrawler.net/). It contains several changes
    from the project which its based on, because of differences of scope between the projects.

1. **How does Link Checker/Stormychecker work?**

    When Curation Module generates its collection reports, all links within them are extracted and saved into
    a database. Link Checker then continuously checks these links and saves their results in the database. There
    are approximately 5 million links, so linkchecker never stops. It takes approximately two months to go over all the links.
    When its finished, it restarts, so that the results can stay actual.
    
1. **What request method does Link Checker use?**

    Link Checker always sends a `HEAD` request first. If it is unsuccessful from whatever reason, it tries
    a `GET` request. However, it doesn't read the response payload in case of `GET`. All info is extracted from
    the status code and the headers.
    
1. **Does Link Checker follow redirects?**

    Yes it does. It even records how many redirects the link points to. 
    However, there is a hard limit of 5 redirects.
    
1. **What is 0 status code?**

    Originally, we decided to regard any link, of which we can't get
    a response to have the 0 status code. This bundled up links with 
    many different problems into one and is not very clear. Therefore, 
    we plan to get rid of the 0 status code. We will change the classification
    system from status codes into categories to make the results more transparent.

1. **What categories are there?**

    Currently there are only 3 categories: 
    
    1. Ok
    2. Broken
    3. Undetermined
    
    They are color coded as green, red and yellow respectively. 
    However, we plan to change the classification
    system from status codes into categories to make the results more transparent.
    So there will be more categories in the future. 
    
1. **Curation Module reports my links wrong. What should I do?**

    If you suspect the reason for the reports being wrong is the Link Checker and your links work fine,
    please create an issue on our [github page](https://github.com/clarin-eric/clarin-curation-module/issues).
    
1. **Byte size of my link is shown null but the link has a correct response body. What's wrong?**

    The HTTP Header `Content-Length` is taken as the single source of truth for the byte size. `HEAD`
    requests don't contain a response payload by definition and Link Checker doesn't read the payload of
    `GET` requests to save bandwidth and time. Therefore the only reliable source is the `Content-Lenght` header.
    Please set it correctly wherever you can on your servers.
    
1. **Curation Module shows my links as broken, but they are not, what's wrong?**

    If the status code of these broken links are 0, then we are aware of this issue and it will be fixed
    when we change the classification system from status codes into 
    categories to make the results more transparent. If not, please create an issue on 
    our [github page](https://github.com/clarin-eric/clarin-curation-module/issues), 
    so that we can investigate more closely.
    
1. **What features are yet to be implemented?**

    1. Change classification system from status codes into categories
    2. Show historical results for the links (a list of every check of a particular link)
    3. A csv file containing all the links in a particular link checker statistics view (it currently loads only 100 links)
    4. A fast lane for links to check them as soon as possible (to avoid waiting 2 months to check a link again)
    
    If you wish to suggest a feature, feel free to do so on our [github page](https://github.com/clarin-eric/clarin-curation-module/issues).
    
1. **Link Checker is causing high loads on my servers, What should I do?**

    Link Checker respects `robots.txt` if the server provides it. If the server doesn't provide
    any crawl delay information on its `robots.txt`, it will send requests as fast as it can 
    but never more than one at a time. We absolutely want to avoid causing high loads
    on your servers, so please get in touch as fast as possible, so we can fix the issue.  
    
1. **Where does "Expected Content Type" come from?**

    It is extracted from CMD Records. It is however not specified for all links.
    
1. **I have more questions. Where can I ask them?**

    Feel free to ask any questions by creating issues on our [github page](https://github.com/clarin-eric/clarin-curation-module/issues).
    
1. **Where can I report issues?**

    Feel free to report issues on our [github page](https://github.com/clarin-eric/clarin-curation-module/issues).
