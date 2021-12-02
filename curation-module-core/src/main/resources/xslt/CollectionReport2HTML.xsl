<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/collection-report">
		<xsl:variable name="collectionName">
			<xsl:value-of select="./file-section/provider" />
		</xsl:variable>
		<html>
			<head>
			</head>
			<body>
				<div id="creation-time">
					created at
					<xsl:value-of select="./@creation-time" />
				</div>
				<div id="download">
					download as
					<a>
						<xsl:attribute name="href">
					    <xsl:text>/download/xml/collections/</xsl:text>
					    <xsl:value-of select="$collectionName" />
				    </xsl:attribute>
						<xsl:text> xml</xsl:text>
					</a>
					<a>
						<xsl:attribute name="href">
                   <xsl:text>/download/json/collections/</xsl:text>
                   <xsl:value-of select="$collectionName" />
                </xsl:attribute>
						<xsl:text> json</xsl:text>
					</a>
				</div>
				<br />
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

				<h2>File Section</h2>
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

				<h2>Header Section</h2>
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
							select="./header-section/profiles/profiles">
							<xsl:sort select="./@score" data-type="number"
								order="descending" />
							<xsl:sort select="./@count" data-type="number"
								order="descending" />
							<xsl:variable name="profileID">
								<xsl:value-of select="./@name" />
							</xsl:variable>
							<tr>
								<td>
									<a>
										<xsl:attribute name="href">
							<xsl:text>/profile/</xsl:text>
	                    	<xsl:value-of
											select="translate(@name,'.:','__')"></xsl:value-of>
	                    	<xsl:text>.html</xsl:text>
	                    </xsl:attribute>
										<xsl:value-of select="@name"></xsl:value-of>
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

				<h2>Facet Section</h2>
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
										select="format-number(./facet-section/coverage,'0.0%')" />
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

				<h2>ResourceProxy Section</h2>
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

				<h2>XML Validation Section</h2>
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

				<h2>XML Populated Section</h2>
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

				<h2>URL Validation Section</h2>
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
							select="./url-validation-section/statistics/category">
							<xsl:sort select="@category" />

							<xsl:variable name="category">
								<xsl:value-of select="./@category" />
							</xsl:variable>
							<xsl:variable name="color">
								<xsl:value-of select="./@colorCode" />
							</xsl:variable>
							<tr style="background-color:{$color}">
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
					<h2>Invalid Files Section</h2>
					<ol>
						<xsl:for-each select="./invalid-files/file">
							<!--<li><font color="#dbd839"><xsl:copy-of select="." />, reason: 
								<xsl:value-of select="./@reason"/></font></li> -->
							<li>
								Invalid file:
								<a>
									<xsl:attribute name="href"><xsl:value-of
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