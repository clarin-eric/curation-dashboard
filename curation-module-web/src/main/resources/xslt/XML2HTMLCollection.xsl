<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/collection-report">
<html>
	<head>
		<link rel="stylesheet" type="text/css" href="./VAADIN/themes/mytheme/xsltStyle.css?v=7.6.4" />
	</head>
	<body>
		<h1>Collection Report</h1>
		
		<h3>Collection name: <xsl:value-of select="./file-section/provider"/></h3>
		
		
		<p>Total Score: <xsl:value-of select="./@score"/> out of <xsl:value-of select="./@col-max-score"/></p>
		<p>Average Score: <xsl:value-of select="./@avg-score"/> out of <xsl:value-of select="./@ins-max-score"/></p>
		<p>Maximal score in collection: <xsl:value-of select="./@max-score"/></p>
		<p>Minimal score in collection: <xsl:value-of select="./@min-score"/></p>
		
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
					facet-coverage: <xsl:value-of select="./facet-section/coverage"/>
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
		<p>Percentage of valid Records: <xsl:value-of select="./xml-validation-section/avgRateOfValidRecords"/></p>

		<hr/>

		<h2>XML Populated Section</h2>
		<p>Total number of XML elements: <xsl:value-of select="./xml-populated-section/totNumOfXMLElements"/></p>
		<p>Average number of XML elements: <xsl:value-of select="./xml-populated-section/avgNumOfXMLElements"/></p>
		<p>Total number of simple XML elements: <xsl:value-of select="./xml-populated-section/totNumOfXMLSimpleElements"/></p>
		<p>Average number of simple XML elements: <xsl:value-of select="./xml-populated-section/avgNumOfXMLSimpleElements"/></p>
		<p>Total number of empty XML elements: <xsl:value-of select="./xml-populated-section/totNumOfXMLEmptyElement"/></p>
		<p>Average number of empty XML elements: <xsl:value-of select="./xml-populated-section/avgXMLEmptyElement"/></p>
		<p>Average rate of populated elements: <xsl:value-of select="./xml-populated-section/avgRateOfPopulatedElements"/>%</p>
		
		<hr/>

		<h2>URL Validation Section</h2>
		<p>Total number of links: <xsl:value-of select="./url-validation-section/totNumOfLinks"/></p>
		<p>Average number of links: <xsl:value-of select="./url-validation-section/avgNumOfLinks"/></p>
		<p>Total number of unique links: <xsl:value-of select="./url-validation-section/totNumOfUniqueLinks"/></p>
		<p>Average number of unique links: <xsl:value-of select="./url-validation-section/avgNumOfUniqueLinks"/></p>
		<p>Total number of resourceProxy links: <xsl:value-of select="./url-validation-section/totNumOfResProxiesLinks"/></p>
		<p>Average number of resourceProxy links: <xsl:value-of select="./url-validation-section/avgNumOfResProxiesLinks"/></p>
		<p>Total number of broken links: <xsl:value-of select="./url-validation-section/totNumOfBrokenLinks"/></p>
		<p>Average number of broken links: <xsl:value-of select="./url-validation-section/avgNumOfBrokenLinks"/></p>
		<p>Average number of valid links: <xsl:value-of select="./url-validation-section/avgNumOfValidLinks"/></p>

		<hr/>
		<h2>Single URL Section</h2>
		<table border="1" cellpadding="1" cellspacing="1">
			<thead>
				<tr>
					<th scope="col">url</th>
					<th scope="col">message</th>
					<th scope="col">http-status</th>
					<th scope="col">content-type</th>
					<th scope="col">byte-size</th>
					<th scope="col">request-duration</th>
					<th scope="col">timestamp</th>
				</tr>
			</thead>
			<tbody>
				<xsl:for-each select="./single-url-report/url">
					<tr>
						<td><xsl:copy-of select="."/></td>
						<td><xsl:value-of select="./@message" /></td>
						<td><xsl:value-of select="./@http-status" /></td>
						<td><xsl:value-of select="./@content-type" /></td>
						<td><xsl:value-of select="./@byte-size" /></td>
						<td><xsl:value-of select="./@request-duration" /></td>
						<td><xsl:value-of select="./@timestamp" /></td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>

		<xsl:if test="./invalid-records/record">
			<hr/>	
			<h2>Invalid Records Section</h2>
			 <ol>
			  <xsl:for-each select="./invalid-records/record">
			  <!--<li><font color="#dbd839"><xsl:copy-of select="." />, reason: <xsl:value-of select="./@reason"/></font></li>-->
			  <li><font color="#dbd839">Invalid file: <xsl:copy-of select="." /><br/>Reason: <xsl:value-of select="./@reason"/></font></li>
			  </xsl:for-each>
			</ol>
		</xsl:if>
	</body>
</html>
</xsl:template>
</xsl:stylesheet>