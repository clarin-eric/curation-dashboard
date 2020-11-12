# Clarin Curation Module

Here is the current deployed instance of Curation Module: https://curate.acdh.oeaw.ac.at/

The goal of this project is to implement software component for curation and quality assessment which can be integrated in the CLARINs VLO workflow. Project is initialized by Metadata Curation Task Force. Specification for the Curation Module is based on the Metadata Quality Assessement Service proposal. Curation Module validates and normalizes single MD records, repositories and profiles, to assess their quality and to produce reports with different information for different actors in VLO workflow. For implementation this project will use some of the existing CLARIN components. 

### curation module core
Usable as stand-alone application to generate instance/collection reports and as required API in the curation web module

### curation module web
Deployable web application

### link checker
Stand-alone application for permanent checking of http links. See [this separate github repository](https://github.com/acdh-oeaw/stormychecker) for details
