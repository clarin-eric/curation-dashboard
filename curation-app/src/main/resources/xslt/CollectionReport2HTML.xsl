<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/collectionReport">
		<xsl:variable name="collectionName">
			<xsl:value-of select="./fileReport/provider" />
		</xsl:variable>
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
					    <xsl:value-of select="$collectionName" />
				    </xsl:attribute>
						<xsl:text>xml</xsl:text>
					</a>
					<xsl:text> </xsl:text>
					<a>
						<xsl:attribute name="href">
                   <xsl:text>/download/collection/</xsl:text>
                   <xsl:value-of select="$collectionName" />
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
						select="replace($collectionName,'_',' ')" />
				</h3>
				<p>
					Total Score:
					<xsl:value-of select="format-number(./@aggregatedScore,'0.00')" />
					out of
					<xsl:value-of select="format-number(./@aggregatedMaxScore,'0.00')" />
				</p>
				<p>
					Score percentage:
					<xsl:value-of
						select="format-number(./@scorePercentage,'0.0%')" />
				</p>
				<p>
					Average Score:
					<xsl:value-of
						select="format-number(./@avgScore,'0.00')" />
					out of
					<xsl:value-of
						select="format-number(./@insMaxScore,'0.00')" />
				</p>
				<p>
					Maximal score in collection:
					<xsl:value-of
						select="format-number(./@maxScore,'0.00')" />
				</p>
				<p>
					Minimal score in collection:
					<xsl:value-of
						select="format-number(./@minScore,'0.00')" />
				</p>

				<hr />
				<details>
					<summary>
						<h2>File Section</h2>
					</summary>
					<p>General information on the number of files and the file size.</p>
				</details>
				<p>
					Number of files:
					<xsl:value-of select="./fileReport/numOfFiles" />
				</p>
				<p>
					Total size:
					<xsl:value-of select="./fileReport/size" />
					B
				</p>
				<p>
					Average size:
					<xsl:value-of select="./fileReport/avgFileSize" />
					B
				</p>
				<p>
					Minimal file size:
					<xsl:value-of select="./fileReport/minFileSize" />
					B
				</p>
				<p>
					Maximal file size:
					<xsl:value-of select="./fileReport/maxFileSize" />
					B
				</p>

				<hr />

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
									select="./headerReport/totNumOfProfiles" />
							</td>
						</tr>
					</tfoot>
					<tbody>
						<xsl:for-each
							select="./headerReport/profiles/profile">
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
										select="format-number(./facetReport/@avgScore,'0.0%')" />
								</b>
							</td>
						</tr>
					</tfoot>
					<tbody>
						<xsl:for-each select="./facetReport/facets/facet">
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

				<hr />

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
				<p>
					Total number of resource proxies:
					<xsl:value-of
						select="./resProxyReport/totNumOfResProxies" />
				</p>
				<p>
					Average number of resource proxies:
					<xsl:value-of
						select="format-number(./resProxyReport/avgNumOfResProxies,'0.00')" />
				</p>
				<p>
					Total number of resource proxies with MIME:
					<xsl:value-of
						select="./resProxyReport/totNumOfResourcesWithMime" />
				</p>
				<p>
					Average number of resource proxies with MIME:
					<xsl:value-of
						select="format-number(./resProxyReport/avgNumOfResourcesWithMime,'0.00')" />
				</p>
				<p>
					Total number of resource proxies with reference:
					<xsl:value-of
						select="./resProxyReport/totNumOfResProxiesWithReference" />
				</p>
				<p>
					Average number of resource proxies with references:
					<xsl:value-of
						select="format-number(./resProxyReport/avgNumOfResProxiesWithReference,'0.00')" />
				</p>

				<hr />

				<details>
					<summary>
						<h2>XML Validation Section</h2>
					</summary>
					<p>The XML validation section shows the result of a simple
						validation of each CMD file against its profile. </p>
				</details>
				<p>
					Number of processable Records:
					<xsl:value-of
						select="./fileReport/totNumOfFilesProcessable" />
				</p>
				<p>
					Number of XML valid Records:
					<xsl:value-of
						select="./xmlValidityReport/totNumOfValidRecords" />
				</p>
				<p>
					Ratio XML valid Records:
					<xsl:value-of
						select="format-number(./xmlValidityReport/@avgScoreValid,'0.0%')" />
				</p>

				<hr />

				<details>
					<summary>
						<h2>XML Populated Section</h2>
					</summary>
					<p>The XML populated section shows information on the number of xml
						elements and the fact if these elements are conatining data. </p>
				</details>
				<p>
					Total number of XML elements:
					<xsl:value-of
						select="./xmlPopulationReport/totNumOfXMLElements" />
				</p>
				<p>
					Average number of XML elements:
					<xsl:value-of
						select="format-number(./xmlPopulationReport/avgNumOfXMLElements,'0.00')" />
				</p>
				<p>
					Total number of simple XML elements:
					<xsl:value-of
						select="./xmlPopulationReport/totNumOfXMLSimpleElements" />
				</p>
				<p>
					Average number of simple XML elements:
					<xsl:value-of
						select="format-number(./xmlPopulationReport/avgNumOfXMLSimpleElements,'0.00')" />
				</p>
				<p>
					Total number of empty XML elements:
					<xsl:value-of
						select="./xmlPopulationReport/totNumOfXMLEmptyElement" />
				</p>
				<p>
					Average number of empty XML elements:
					<xsl:value-of
						select="format-number(./xmlPopulationReport/avgXMLEmptyElement,'0.00')" />
				</p>
				<p>
					Average rate of populated elements:
					<xsl:value-of
						select="format-number(./xmlPopulationReport/avgRateOfPopulatedElements,'0.0%')" />
				</p>

				<hr />

				<details>
					<summary>
						<h2>URL Validation Section</h2>
					</summary>
					<p>The URL validation section shows information on the number of
						links and the results of link checking for the links which
						have been checked so far.
					</p>
				</details>
				<p>
					Total number of links:
					<xsl:value-of
						select="./linkcheckerReport/totNumOfLinks" />
				</p>
				<p>
					Average number of links:
					<xsl:value-of
						select="format-number(./linkcheckerReport/avgNumOfLinks,'0.00')" />
				</p>
				<p>
					Total number of unique links:
					<xsl:value-of
						select="./linkcheckerReport/totNumOfUniqueLinks" />
				</p>
           <p>
               Average number of unique links:
               <xsl:value-of
                  select="format-number(./linkcheckerReport/avgNumOfUniqueLinks,'0.00')" />
            </p>
				<p>
					Total number of checked links:
					<xsl:value-of
						select="./linkcheckerReport/totNumOfCheckedLinks" />
				</p>
				<p>
					Ratio of valid links:
					<xsl:value-of
						select="format-number(./linkcheckerReport/ratioOfValidLinks,'0.0%')" />
				</p>

            <xsl:if test="./linkcheckerReport/linkchecker/statistics">
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
								select="./linkcheckerReport/linkchecker/statistics">
								<xsl:variable name="category">
									<xsl:value-of select="./@category" />
								</xsl:variable>
	
								<tr class="{$category}">
									<td align="right">
										<a href="/statistics/{$collectionName}/{$category}">
											<xsl:copy-of select="$category" />
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
                                       select="./@origin" /></xsl:attribute>
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
                                          <xsl:value-of select="."></xsl:value-of>
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
</xsl:stylesheet>