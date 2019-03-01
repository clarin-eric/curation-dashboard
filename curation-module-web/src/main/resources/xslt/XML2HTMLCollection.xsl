<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/collection-report">
<html>
	<head>
		<link rel="stylesheet" type="text/css" href="./VAADIN/themes/mytheme/xsltStyle.css?v=7.6.4" />
	</head>
	<body>
		<h1>Collection Report</h1>
		<xsl:variable name="collectionName"><xsl:value-of select="./file-section/provider"/></xsl:variable>

		<h3>Collection name: <xsl:copy-of select="$collectionName" /></h3>

		<p>Total Score: <xsl:value-of select="./@score"/> out of <xsl:value-of select="./@col-max-score"/></p>
		<p>Score percentage: <xsl:value-of select="format-number(./@score-percentage,'##.#%')"/></p>
		<p>Average Score: <xsl:value-of select="./@avg-score"/> out of <xsl:value-of select="./@ins-max-score"/></p>
		<p>Maximal score in collection: <xsl:value-of select="./@max-score"/></p>
		<p>Minimal score in collection: <xsl:value-of select="./@min-score"/></p>

		<hr/>

		<p>Timestamp: <xsl:value-of select="./@timeStamp"/></p>

		<hr/>

		<h2>File Section</h2>
		<p>Number of files: <xsl:value-of select="./file-section/numOfFiles"/></p>
		<p>Total size: <xsl:value-of select="./file-section/size"/> B</p>
		<p>Average size: <xsl:value-of select="./file-section/avgSize"/> B</p>
		<p>Minimal file size: <xsl:value-of select="./file-section/minFileSize"/> B</p>
		<p>Maximal file size: <xsl:value-of select="./file-section/maxFileSize"/> B</p>

		<hr/>

		<h2>Header Section</h2>
		<table>
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
					<td colspan="3">Total number of profiles: <xsl:value-of select="./header-section/profiles/@count"/></td>
				</tr>
			</tfoot>
			<tbody>
			<xsl:for-each select="./header-section/profiles/profiles">
				<xsl:sort select="./@score" data-type="number" order="descending"/>
				<xsl:sort select="./@count" data-type="number" order="descending"/>
				<xsl:variable name="profileID"><xsl:value-of select="./@name"/></xsl:variable>
				<tr>
					<td><a href="#!ResultView/profile/id/{$profileID}"><xsl:copy-of select="$profileID"/></a></td>
					<td><xsl:value-of select="./@score"/></td>
					<td><xsl:value-of select="./@count"/></td>
			    </tr>
			</xsl:for-each>
			</tbody>
		</table>

		<h2>Facet Section</h2>
		<table border="1" cellpadding="1" cellspacing="1">
			<thead>
				<tr>
					<th scope="col">name</th>
					<th scope="col">coverage</th>
				</tr>
			</thead>
			<tfoot>
				<tr><td colspan="2"><b>
					facet-coverage: <xsl:value-of select="format-number(./facet-section/coverage,'##.#%')"/>
				</b></td></tr>
			</tfoot>
			<tbody>
			<xsl:for-each select="./facet-section/facets/facet">
			<tr>
				<td><xsl:value-of select="./@name"/></td>
				<td><xsl:value-of select="./@coverage"/></td>
		    </tr>
			</xsl:for-each>
			</tbody>
		</table>

		<hr/>

		<h2>ResourceProxy Section</h2>
		<p>Total number of resource proxies: <xsl:value-of select="./resProxy-section/totNumOfResProxies"/></p>
		<p>Average number of resource proxies: <xsl:value-of select="./resProxy-section/avgNumOfResProxies"/></p>
		<p>Total number of resource proxies with MIME: <xsl:value-of select="./resProxy-section/totNumOfResourcesWithMime"/></p>
		<p>Average number of resource proxies with MIME: <xsl:value-of select="./resProxy-section/avgNumOfResourcesWithMime"/></p>
		<p>Total number of resource proxies with reference: <xsl:value-of select="./resProxy-section/totNumOfResProxiesWithReferences"/></p>
		<p>Average number of resource proxies with references: <xsl:value-of select="./resProxy-section/avgNumOfResProxiesWithReferences"/></p>

		<hr/>

		<h2>XML Validation Section</h2>
		<p>Number of Records: <xsl:value-of select="./xml-validation-section/totNumOfRecords"/></p>
		<p>Number of valid Records: <xsl:value-of select="./xml-validation-section/totNumOfValidRecords"/></p>
		<p>Ratio valid Records: <xsl:value-of select="format-number(./xml-validation-section/ratioOfValidRecords,'##.#%)'"/></p>

		<xsl:if test="./xml-validation-section/invalid-records/record">
			<h3>Invalid Records:</h3>
			<ol>
				<xsl:for-each select="./xml-validation-section/invalid-records/record">
					<li><xsl:value-of select="./@name" /></li>
				</xsl:for-each>
			</ol>
		</xsl:if>

		<hr/>

		<h2>XML Populated Section</h2>
		<p>Total number of XML elements: <xsl:value-of select="./xml-populated-section/totNumOfXMLElements"/></p>
		<p>Average number of XML elements: <xsl:value-of select="./xml-populated-section/avgNumOfXMLElements"/></p>
		<p>Total number of simple XML elements: <xsl:value-of select="./xml-populated-section/totNumOfXMLSimpleElements"/></p>
		<p>Average number of simple XML elements: <xsl:value-of select="./xml-populated-section/avgNumOfXMLSimpleElements"/></p>
		<p>Total number of empty XML elements: <xsl:value-of select="./xml-populated-section/totNumOfXMLEmptyElement"/></p>
		<p>Average number of empty XML elements: <xsl:value-of select="./xml-populated-section/avgXMLEmptyElement"/></p>
		<p>Average rate of populated elements: <xsl:value-of select="format-number(./xml-populated-section/avgRateOfPopulatedElements,'##.#%')"/></p>

		<hr/>

		<h2>URL Validation Section</h2>
		<p>Total number of links: <xsl:value-of select="./url-validation-section/totNumOfLinks"/></p>
		<p>Average number of links: <xsl:value-of select="./url-validation-section/avgNumOfLinks"/></p>
		<p>Total number of unique links: <xsl:value-of select="./url-validation-section/totNumOfUniqueLinks"/></p>
		<p>Total number of checked links: <xsl:value-of select="./url-validation-section/totNumOfCheckedLinks"/></p>
		<p>Total number of undetermined links: <xsl:value-of select="./url-validation-section/totNumOfUndeterminedLinks"/></p>
		<p>Average number of unique links: <xsl:value-of select="./url-validation-section/avgNumOfUniqueLinks"/></p>
		<!--<p>Total number of resourceProxy links: <xsl:value-of select="./url-validation-section/totNumOfResProxiesLinks"/></p>-->
		<!--<p>Average number of resourceProxy links: <xsl:value-of select="./url-validation-section/avgNumOfResProxiesLinks"/></p>-->
		<p>Total number of broken links: <xsl:value-of select="./url-validation-section/totNumOfBrokenLinks"/></p>
		<p>Average number of broken links: <xsl:value-of select="./url-validation-section/avgNumOfBrokenLinks"/></p>
		<p>Ratio of valid links: <xsl:value-of select="format-number(./url-validation-section/ratioOfValidLinks,'##.#%')"/></p>


		<h3>Status Codes Table</h3>

		<h4>Ok</h4>
		<table border="1" cellpadding="1" cellspacing="1">
			<thead>
				<tr>
					<th scope="col">Status</th>
					<th scope="col">Category</th>
					<th scope="col">Count</th>
					<th scope="col">Average Response Duration(ms)</th>
					<th scope="col">Max Response Duration(ms)</th>
				</tr>
			</thead>
			<tbody>
				<xsl:for-each select="./url-validation-section/statistics/status">
					<xsl:sort select="@category"/>
					<xsl:variable name="status"><xsl:value-of select="./@statusCode"/></xsl:variable>
					<xsl:variable name="category"><xsl:value-of select="./@category"/></xsl:variable>
                        <tr>
							<xsl:if test="$category='Ok'">
                            	<td style="background-color:#cbe7cc" align="right"><a href="'#!ResultView/statistics//{$collectionName}/{$status}"><xsl:copy-of select="$status" /></a></td>
								<td style="background-color:#cbe7cc" align="right"><xsl:value-of select="./@category" /></td>
							</xsl:if>
							<xsl:if test="$category='Undetermined'">
								<td style="background-color:#fff7b3" align="right"><a href="'#!ResultView/statistics//{$collectionName}/{$status}"><xsl:copy-of select="$status" /></a></td>
								<td style="background-color:#fff7b3" align="right"><xsl:value-of select="./@category" /></td>
							</xsl:if>
							<xsl:if test="$category='Broken'">
								<td style="background-color:#f2a6a6" align="right"><a href="'#!ResultView/statistics//{$collectionName}/{$status}"><xsl:copy-of select="$status" /></a></td>
								<td style="background-color:#f2a6a6" align="right"><xsl:value-of select="./@category" /></td>
							</xsl:if>
                            <td align="right"><xsl:value-of select="./@count" /></td>
                            <td align="right"><xsl:value-of select="format-number(./@avgRespTime, '###,##0.##')" /></td>
                            <td align="right"><xsl:value-of select="format-number(./@maxRespTime, '###,##0.##')" /></td>
                        </tr>

				</xsl:for-each>
			</tbody>
		</table>







		<hr/>
		<xsl:if test="./invalid-files/file">
			<hr/>
			<h2>Invalid Files Section</h2>
			 <ol>
			  <xsl:for-each select="./invalid-files/file">
			  <!--<li><font color="#dbd839"><xsl:copy-of select="." />, reason: <xsl:value-of select="./@reason"/></font></li>-->
			  <li>Invalid file: <xsl:copy-of select="." /><br/><font color="#dbd839">Reason: <xsl:value-of select="./@reason"/></font></li>
			  </xsl:for-each>
			</ol>
		</xsl:if>
	</body>
</html>
</xsl:template>
</xsl:stylesheet>