# Curation Public Profile Header Service (PPH)

## What it does

The service provides the descriptions of public profiles. A profile description might look like this:

```xml
<profileDescription>
   <id>clarin.eu:cr1:p_1357720977520</id>
   <name>AnnotatedCorpusProfile</name>
   <description>A CMDI profile for annotated text corpus resources.</description>
   <registrationDate>2013-01-31T11:57:12+00:00</registrationDate>
   <creatorName>nalida</creatorName>
   <userId>22</userId>
   <domainName/>
   <ns2:href>http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1357720977520</ns2:href>
   <groupName>CLARIN</groupName>
   <status>DEPRECATED</status>
   <successor>clarin.eu:cr1:p_1442920133046</successor>
   <commentsCount>0</commentsCount>
   <isPublic>true</isPublic>
   <recommended>false</recommended>
   <showInEditor>true</showInEditor>
</profileDescription>
```
This information is stored in a ProfileHeader objects and most of the element informations are available by getter methods. 