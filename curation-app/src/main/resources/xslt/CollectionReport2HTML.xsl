<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/collection-report">
		<xsl:variable name="collectionName">
			<xsl:value-of select="./file-section/provider" />
		</xsl:variable>
		<html>
			<head>
			</head>
			<body>
				<div class="creation-time">
					created at
					<xsl:value-of select="./@creation-time" />
				</div>
				<div class="download">
					download as
					<a>
						<xsl:attribute name="href">
					    <xsl:text>/download/xml/collections/</xsl:text>
					    <xsl:value-of select="$collectionName" />
				    </xsl:attribute>
						<xsl:text>xml</xsl:text>
					</a>
					<xsl:text> </xsl:text>
					<a>
						<xsl:attribute name="href">
                   <xsl:text>/download/json/collections/</xsl:text>
                   <xsl:value-of select="$collectionName" />
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
					<xsl:value-of select="./@score" />
					out of
					<xsl:value-of select="./@col-max-score" />
				</p>
				<p>
					Score percentage:
					<xsl:value-of
						select="format-number(./@score-percentage,'0.0%')" />
				</p>
				<p>
					Average Score:
					<xsl:value-of
						select="format-number(./@avg-score,'0.00')" />
					out of
					<xsl:value-of
						select="format-number(./@ins-max-score,'0.00')" />
				</p>
				<p>
					Maximal score in collection:
					<xsl:value-of
						select="format-number(./@max-score,'0.00')" />
				</p>
				<p>
					Minimal score in collection:
					<xsl:value-of
						select="format-number(./@min-score,'0.00')" />
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
					<xsl:value-of select="./file-section/numOfFiles" />
				</p>
				<p>
					Total size:
					<xsl:value-of select="./file-section/size" />
					B
				</p>
				<p>
					Average size:
					<xsl:value-of select="./file-section/avgSize" />
					B
				</p>
				<p>
					Minimal file size:
					<xsl:value-of select="./file-section/minFileSize" />
					B
				</p>
				<p>
					Maximal file size:
					<xsl:value-of select="./file-section/maxFileSize" />
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
					<caption>Profiles in Collection</caption>
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
									select="./header-section/profiles/@count" />
							</td>
						</tr>
					</tfoot>
					<tbody>
						<xsl:for-each
							select="./header-section/profiles/profile">
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
										select="format-number(./facet-section/avg-score,'0.0%')" />
								</b>
							</td>
						</tr>
					</tfoot>
					<tbody>
						<xsl:for-each select="./facet-section/facets/facet">
							<tr>
								<td>
									<xsl:value-of select="./@name" />
								</td>
								<td class="text-right">
									<xsl:value-of
										select="format-number(./@coverage,'0.0%')" />
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
						select="./resProxy-section/totNumOfResProxies" />
				</p>
				<p>
					Average number of resource proxies:
					<xsl:value-of
						select="format-number(./resProxy-section/avgNumOfResProxies,'0.00')" />
				</p>
				<p>
					Total number of resource proxies with MIME:
					<xsl:value-of
						select="./resProxy-section/totNumOfResourcesWithMime" />
				</p>
				<p>
					Average number of resource proxies with MIME:
					<xsl:value-of
						select="format-number(./resProxy-section/avgNumOfResourcesWithMime,'0.00')" />
				</p>
				<p>
					Total number of resource proxies with reference:
					<xsl:value-of
						select="./resProxy-section/totNumOfResProxiesWithReferences" />
				</p>
				<p>
					Average number of resource proxies with references:
					<xsl:value-of
						select="format-number(./resProxy-section/avgNumOfResProxiesWithReferences,'0.00')" />
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
					Number of Records:
					<xsl:value-of
						select="./xml-validation-section/totNumOfRecords" />
				</p>
				<p>
					Number of valid Records:
					<xsl:value-of
						select="./xml-validation-section/totNumOfValidRecords" />
				</p>
				<p>
					Ratio valid Records:
					<xsl:value-of
						select="format-number(./xml-validation-section/ratioOfValidRecords,'0.0%')" />
				</p>

				<xsl:if
					test="./xml-validation-section/invalid-records/record">
					<h3>Invalid Records:</h3>
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
								select="./xml-validation-section/invalid-records/record">

								<xsl:if test="not(position() > 100)">
									<tr>
										<td>
											<a>
												<xsl:attribute name="href">/record/<xsl:value-of
													select="./@name" /></xsl:attribute>
												<xsl:value-of select="./@name" />
											</a>

										</td>
										<td>
											<button type="button" class="showUrlInfo btn btn-info"
												onClick="toggleInfo(this)">Show</button>
										</td>
										<td>
											<button type="button" class="btn btn-info">
												<xsl:attribute name="onClick">window.open('/curate?url-input=<xsl:value-of
													select="./@name"></xsl:value-of>')</xsl:attribute>
												Validate file
											</button>
										</td>
									</tr>
									<tr hidden="true">
										<td colspan="3">
											<ul>
												<xsl:for-each select="issue">
													<li>
														<xsl:value-of select="."></xsl:value-of>
													</li>
												</xsl:for-each>
											</ul>
										</td>
									</tr>
								</xsl:if>
							</xsl:for-each>
							<xsl:if
								test="count(./xml-validation-section/invalid-records/record) > 100">
								<tr>
									<td colspan="3">[...] complete list in downloadable report</td>
								</tr>
							</xsl:if>
						</tbody>
					</table>
				</xsl:if>
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
						select="./xml-populated-section/totNumOfXMLElements" />
				</p>
				<p>
					Average number of XML elements:
					<xsl:value-of
						select="format-number(./xml-populated-section/avgNumOfXMLElements,'0.00')" />
				</p>
				<p>
					Total number of simple XML elements:
					<xsl:value-of
						select="./xml-populated-section/totNumOfXMLSimpleElements" />
				</p>
				<p>
					Average number of simple XML elements:
					<xsl:value-of
						select="format-number(./xml-populated-section/avgNumOfXMLSimpleElements,'0.00')" />
				</p>
				<p>
					Total number of empty XML elements:
					<xsl:value-of
						select="./xml-populated-section/totNumOfXMLEmptyElement" />
				</p>
				<p>
					Average number of empty XML elements:
					<xsl:value-of
						select="format-number(./xml-populated-section/avgXMLEmptyElement,'0.00')" />
				</p>
				<p>
					Average rate of populated elements:
					<xsl:value-of
						select="format-number(./xml-populated-section/avgRateOfPopulatedElements,'0.0%')" />
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
						select="./url-validation-section/totNumOfLinks" />
				</p>
				<p>
					Average number of links:
					<xsl:value-of
						select="format-number(./url-validation-section/avgNumOfLinks,'0.00')" />
				</p>
				<p>
					Total number of unique links:
					<xsl:value-of
						select="./url-validation-section/totNumOfUniqueLinks" />
				</p>
				<p>
					Total number of checked links:
					<xsl:value-of
						select="./url-validation-section/totNumOfCheckedLinks" />
				</p>
				<p>
					Total number of undetermined links:
					<xsl:value-of
						select="./url-validation-section/totNumOfUndeterminedLinks" />
				</p>
				<p>
					Average number of unique links:
					<xsl:value-of
						select="format-number(./url-validation-section/avgNumOfUniqueLinks,'0.00')" />
				</p>
				<!--<p>Total number of resourceProxy links: <xsl:value-of select="./url-validation-section/totNumOfResProxiesLinks"/></p> -->
				<!--<p>Average number of resourceProxy links: <xsl:value-of select="./url-validation-section/avgNumOfResProxiesLinks"/></p> -->
				<p>
					Total number of broken links:
					<xsl:value-of
						select="./url-validation-section/totNumOfBrokenLinks" />
				</p>
				<p>
					Average number of broken links:
					<xsl:value-of
						select="format-number(./url-validation-section/avgNumOfBrokenLinks,'0.00')" />
				</p>
				<p>
					Ratio of valid links:
					<xsl:value-of
						select="format-number(./url-validation-section/ratioOfValidLinks,'0.0%')" />
				</p>


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
							select="./url-validation-section/linkchecker/statistics">
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


				<hr />
				<xsl:if test="./invalid-files/file">
					<hr />
					<details>
						<summary>
							<h2>Invalid Files Section</h2>
						</summary>
						<p>The invalid files section shows the number of non processed
							CMD-files of a collection and the reason for not processing these
							files.</p>
					</details>
					<ol>
						<xsl:for-each select="./invalid-files/file">
							<!--<li><font color="#dbd839"><xsl:copy-of select="." />, reason: 
								<xsl:value-of select="./@reason"/></font></li> -->
							<li>
								Invalid file:
								<a>
									<xsl:attribute name="href">/record/<xsl:value-of
										select="." /></xsl:attribute>
									<xsl:value-of select="." />
								</a>
								<br />
								<font color="#dbd839">
									Reason:
									<xsl:value-of select="./@reason" />
								</font>
							</li>
						</xsl:for-each>
					</ol>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>