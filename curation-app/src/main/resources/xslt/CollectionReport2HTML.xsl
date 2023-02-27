<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/collectionReport">
		<html>
			<head>
			</head>
			<body>
				<div class="creationTime">
					created at
					<xsl:value-of select="./@creationTime" />
				</div>
				<div class="download">
					download as
					<a>
						<xsl:attribute name="href">
					    <xsl:text>/download/collection/</xsl:text>
					    <xsl:value-of select="//fileReport/provider" />
				    </xsl:attribute>
						<xsl:text>xml</xsl:text>
					</a>
					<xsl:text> </xsl:text>
					<a>
						<xsl:attribute name="href">
                   <xsl:text>/download/collection/</xsl:text>
                   <xsl:value-of select="//fileReport/provider" />
                   <xsl:text>?format=json</xsl:text>
                </xsl:attribute>
						<xsl:text>json</xsl:text>
					</a>
				</div>
				<div class="clear" />
				<h1>Collection Report</h1>
				<h3>
					Collection name:
					<xsl:value-of
						select="replace(//fileReport/provider,'_',' ')" />
				</h3>
            <xsl:call-template name="scoreTable" />

				<hr />
            <xsl:apply-templates select="fileReport" />
				<hr />
            <xsl:apply-templates select="headerReport" />
            <hr />
            <xsl:apply-templates select="facetReport" />
            <hr />
            <xsl:apply-templates select="resProxyReport" />
            <hr />
            <xsl:apply-templates select="xmlValidityReport" />
            <hr />
            <xsl:apply-templates select="xmlPopulationReport" />
            <hr />
            <xsl:apply-templates select="linkcheckerReport" />
				<hr />
				
           <xsl:if test="./recordDetails/record">
            <hr />
            <details>
               <summary>
                  <h2>Record details:</h2>
                  </summary>
                  <p>The record details section shows the particalarities of each record as far as they're of importance for the data provider.</p>
               </details>                  
               <table class="reportTable">
                  <thead>
                     <tr>
                        <th>File</th>
                        <th>Info</th>
                        <th>Validate</th>
                     </tr>
                  </thead>
                  <tbody>
                     <xsl:for-each
                        select="./recordDetails/record">

                        <xsl:if test="not(position() > 100)">
                           <tr>
                              <td>
                                 <a>
                                    <xsl:attribute name="href">/record/<xsl:value-of
                                       select="replace(./@origin, '/', '#')" /></xsl:attribute>
                                    <xsl:value-of select="./@origin" />
                                 </a>

                              </td>
                              <td>
                                 <button type="button" class="showUrlInfo btn btn-info"
                                    onClick="toggleInfo(this)">Show</button>
                              </td>
                              <td>
                                 <button type="button" class="btn btn-info">
                                    <xsl:attribute name="onClick">window.open('/curate?url-input=<xsl:value-of
                                       select="./@origin"></xsl:value-of>')</xsl:attribute>
                                    Validate file
                                 </button>
                              </td>
                           </tr>
                           <tr hidden="true">
                              <td colspan="3">
                                 <ul>
                                    <xsl:for-each select="detail">
                                       <li>
                                          Severity: <xsl:value-of select="./severity"></xsl:value-of>, 
                                          Segment: <xsl:value-of select="./segment"></xsl:value-of>, 
                                          Message:  <xsl:value-of select="./message"></xsl:value-of>
                                       </li>
                                    </xsl:for-each>
                                 </ul>
                              </td>
                           </tr>
                        </xsl:if>
                     </xsl:for-each>
                     <xsl:if
                        test="count(./recordDetails/record) > 100">
                        <tr>
                           <td colspan="3">[...] complete list in downloadable report</td>
                        </tr>
                     </xsl:if>
                  </tbody>
               </table>
            </xsl:if>
			</body>
		</html>
	</xsl:template>
	
	<!-- fileReport -->
   <xsl:template match="fileReport">
            <details>
               <summary>
                  <h2>File Section</h2>
               </summary>
               <p>General information on the number of files and the file size.</p>
            </details>
            <xsl:call-template name="scoreTable" />
            <xsl:value-of select="./numOfFiles" />
            <p>
               Number of files:
               <xsl:value-of select="./numOfFiles" />
            </p>
            <p>
               Total size:
               <xsl:value-of select="./size" />
               B
            </p>
            <p>
               Average size:
               <xsl:value-of select="./avgFileSize" />
               B
            </p>
            <p>
               Minimal file size:
               <xsl:value-of select="./minFileSize" />
               B
            </p>
            <p>
               Maximal file size:
               <xsl:value-of select="./maxFileSize" />
               B
            </p>
   </xsl:template>
   
   <!-- headerReport -->
   <xsl:template match="headerReport">
            <details>
               <summary>
                  <h2>Header Section</h2>
               </summary>
               <p>
                  The header section shows information on the profile usage in the
                  collection.
                  <br />
                  Important note: the score of this section differs from the score
                  of the underlying profile. For more information
                  on scoring have a look at the
                  <a href="/faq">FAQ</a>
                  , please.
               </p>
            </details>
            <xsl:call-template name="scoreTable" /> 
            <table class="reportTable">
               <thead>
                  <tr>
                     <th>ID</th>
                     <th>Score</th>
                     <th>Count</th>
                  </tr>
               </thead>
               <tfoot>
                  <tr>
                     <td colspan="3">
                        Total number of profiles:
                        <xsl:value-of
                           select="./totNumOfProfiles" />
                     </td>
                  </tr>
               </tfoot>
               <tbody>
                  <xsl:for-each
                     select="./profiles/profile">
                     <xsl:sort select="./@score" data-type="number"
                        order="descending" />
                     <xsl:sort select="./@count" data-type="number"
                        order="descending" />
                     <xsl:variable name="profileID">
                        <xsl:value-of select="./@profileId" />
                     </xsl:variable>
                     <tr>
                        <td>
                           <a>
                              <xsl:attribute name="href">
                     <xsl:text>/profile/</xsl:text>
                        <xsl:value-of
                                 select="translate(./@profileId,'.:','__')"></xsl:value-of>
                        <xsl:text>.html</xsl:text>
                       </xsl:attribute>
                              <xsl:value-of select="./@profileId"></xsl:value-of>
                           </a>
                        </td>
                        <td class='text-right'>
                           <xsl:value-of
                              select="format-number(./@score,'0.00')" />
                        </td>
                        <td class='text-right'>
                           <xsl:value-of select="./@count" />
                        </td>
                     </tr>
                  </xsl:for-each>
               </tbody>
            </table>   
   </xsl:template>
   
   <!-- facetReport -->
   <xsl:template match="facetReport" >
            <details>
               <summary>
                  <h2>Facet Section</h2>
               </summary>
               <p>The facet section shows the facet coverage within the
                  collection. It's quite evident that the facet coverage of a
                  certain CMD file can't be higher than those of the profile it is
                  based on.
               </p>
            </details>
            <xsl:call-template name="scoreTable" />
            <table class="reportTable">
               <thead>
                  <tr>
                     <th scope="col">name</th>
                     <th scope="col">coverage</th>
                  </tr>
               </thead>
               <tfoot>
                  <tr>
                     <td colspan="2">
                        <b>
                           facet-coverage:
                           <xsl:value-of
                              select="format-number(./@avgScoreProcessable,'0.0%')" />
                        </b>
                     </td>
                  </tr>
               </tfoot>
               <tbody>
                  <xsl:for-each select="./facets/facet">
                     <tr>
                        <td>
                           <xsl:value-of select="./@name" />
                        </td>
                        <td class="text-right">
                           <xsl:value-of
                              select="format-number(./@avgCoverage,'0.0%')" />
                        </td>
                     </tr>
                  </xsl:for-each>
               </tbody>
            </table>   
   </xsl:template>
   
   <!-- resProxyReport -->
   <xsl:template match="resProxyReport">
            <details>
               <summary>
                  <h2>ResourceProxy Section</h2>
               </summary>
               <p>The resource proxy section shows information on the number of
                  resource proxies on the kind (the mime type) of resources.
                  A resource proxy is a link to an external resource, described by
                  the CMD file.
               </p>
            </details>
            <xsl:call-template name="scoreTable" />
            <p>
               Total number of resource proxies:
               <xsl:value-of
                  select="./totNumOfResProxies" />
            </p>
            <p>
               Average number of resource proxies:
               <xsl:value-of
                  select="format-number(./avgNumOfResProxies,'0.00')" />
            </p>
            <p>
               Total number of resource proxies with MIME:
               <xsl:value-of
                  select="./totNumOfResProxiesWithMime" />
            </p>
            <p>
               Average number of resource proxies with MIME:
               <xsl:value-of
                  select="format-number(./avgNumOfResProxiesWithMime,'0.00')" />
            </p>
            <p>
               Total number of resource proxies with reference:
               <xsl:value-of
                  select="./totNumOfResProxiesWithReference" />
            </p>
            <p>
               Average number of resource proxies with references:
               <xsl:value-of
                  select="format-number(./avgNumOfResProxiesWithReference,'0.00')" />
            </p>   
   </xsl:template>
   
   <!-- xmlValidityReport -->
   <xsl:template match="xmlValidityReport">
            <details>
               <summary>
                  <h2>XML Validation Section</h2>
               </summary>
               <p>The XML validation section shows the result of a simple
                  validation of each CMD file against its profile. </p>
            </details>
            <xsl:call-template name="scoreTable" />
            <p>
               Number of XML processable Records:
               <xsl:value-of
                  select="//fileReport/totNumOfFilesProcessable" />
            </p>
            <p>
               Number of XML valid Records:
               <xsl:value-of
                  select="./totNumOfValidRecords" />
            </p>
            <p>
               Ratio XML valid Records:
               <xsl:value-of
                  select="format-number(./@avgScoreProcessable,'0.0%')" />
            </p>   
   </xsl:template>
   
   <!-- xmlPopulationReport -->
   <xsl:template match="xmlPopulationReport">
            <details>
               <summary>
                  <h2>XML Populated Section</h2>
               </summary>
               <p>The XML populated section shows information on the number of xml
                  elements and the fact if these elements are conatining data. </p>
            </details>
            <xsl:call-template name="scoreTable" />
            <p>
               Total number of XML elements:
               <xsl:value-of
                  select="./totNumOfXMLElements" />
            </p>
            <p>
               Average number of XML elements:
               <xsl:value-of
                  select="format-number(./avgNumOfXMLElements,'0.00')" />
            </p>
            <p>
               Total number of simple XML elements:
               <xsl:value-of
                  select="./totNumOfXMLSimpleElements" />
            </p>
            <p>
               Average number of simple XML elements:
               <xsl:value-of
                  select="format-number(./avgNumOfXMLSimpleElements,'0.00')" />
            </p>
            <p>
               Total number of empty XML elements:
               <xsl:value-of
                  select="./totNumOfXMLEmptyElements" />
            </p>
            <p>
               Average number of empty XML elements:
               <xsl:value-of
                  select="format-number(./avgXMLEmptyElements,'0.00')" />
            </p>
            <p>
               Average rate of populated elements:
               <xsl:value-of
                  select="format-number(./avgRateOfPopulatedElements,'0.0%')" />
            </p>   
   </xsl:template>
   
   <!-- linkcheckerReport -->
   <xsl:template match="linkcheckerReport">
            <details>
               <summary>
                  <h2>URL Validation Section</h2>
               </summary>
               <p>The URL validation section shows information on the number of
                  links and the results of link checking for the links which
                  have been checked so far.
               </p>
            </details>
            <xsl:call-template name="scoreTable" />
            <p>
               Total number of links:
               <xsl:value-of
                  select="./totNumOfLinks" />
            </p>
            <p>
               Average number of links:
               <xsl:value-of
                  select="format-number(./avgNumOfLinks,'0.00')" />
            </p>
            <p>
               Total number of unique links:
               <xsl:value-of
                  select="./totNumOfUniqueLinks" />
            </p>
           <p>
               Average number of unique links:
               <xsl:value-of
                  select="format-number(./avgNumOfUniqueLinks,'0.00')" />
            </p>
            <p>
               Total number of checked links:
               <xsl:value-of
                  select="./totNumOfCheckedLinks" />
            </p>
            <p>
               Ratio of valid links:
               <xsl:value-of
                  select="format-number(./ratioOfValidLinks,'0.0%')" />
            </p>

            <xsl:if test="./linkchecker/statistics">
               <h3>Link Checking Results</h3>
   
               <table class="reportTable">
                  <thead>
                     <tr>
                        <th scope="col">Category</th>
                        <th scope="col">Count</th>
                        <th scope="col">Average Response Duration(ms)</th>
                        <th scope="col">Max Response Duration(ms)</th>
                     </tr>
                  </thead>
                  <tbody>
                     <xsl:for-each
                        select="./linkchecker/statistics">
   
                        <tr>
                        <xsl:attribute name="class">
                        <xsl:value-of select="./@category" />
                        </xsl:attribute>
                           <td align="right">
                              <a>
                              <xsl:attribute name="href">
                              <xsl:text>/linkchecker/</xsl:text>
                              <xsl:value-of select="//fileReport/provider" />
                              <xsl:text>#</xsl:text>
                              <xsl:value-of select="./@category" />
                              </xsl:attribute>
                              <xsl:value-of select="//fileReport/provider" />
                              </a>
                           </td>
   
                           <td align="right">
                              <xsl:value-of select="./@count" />
                           </td>
                           <td align="right">
                              <xsl:value-of
                                 select="format-number(./@avgRespTime, '###,##0.##')" />
                           </td>
                           <td align="right">
                              <xsl:value-of
                                 select="format-number(./@maxRespTime, '###,##0.##')" />
                           </td>
                        </tr>   
                     </xsl:for-each>
                  </tbody>
   
               </table>
           </xsl:if>   
   </xsl:template>
   
   <!-- scoreTable -->
	<xsl:template name="scoreTable">
	<table class="scoreTable">
	  <thead>
	     <tr>
	        <td></td>
           <td>Processable</td>
	        <td>All</td>
	     </tr>
	  </thead>
	  <tbody>
	     <tr>
	        <td>Aggregated score</td>
	        <td><xsl:value-of select="format-number(./@aggregatedScore, '0.00')" /></td>
	        <td><xsl:value-of select="format-number(./@aggregatedScore, '0.00')" /></td>
	     </tr>
	     <tr>
	        <td>Aggregated maximal score</td>
	        <td><xsl:value-of select="format-number(./@aggregatedMaxScoreProcessable, '0.00')" /></td>
	        <td><xsl:value-of select="format-number(./@aggregatedMaxScoreAll, '0.00')" /></td>
	     </tr>
        <tr>
           <td>Score percentage</td>
           <td><xsl:value-of select="format-number(./@scorePercentageProcessable, '0.0%')" /></td>
           <td><xsl:value-of select="format-number(./@scorePercentageAll, '0.0%')" /></td>
        </tr>
        <tr>
           <td>Average score</td>
           <td><xsl:value-of select="format-number(./@avgScoreProcessable, '0.00')" /></td>
           <td><xsl:value-of select="format-number(./@avgScoreAll, '0.00')" /></td>
        </tr>
	  </tbody>	  
	</table>	
	</xsl:template>
</xsl:stylesheet>