<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/collection-report">
<html>
<body style="font-family: ‘Lucida Sans Unicode’, ‘Lucida Grande’, sans-serif;">
	<h1>Report for the collection: <xsl:value-of select="./file-section/provider"/></h1>
	
	
	<p>Total Score: <xsl:value-of select="./score"/> from <xsl:value-of select="./@max-score-collection"/></p>
	<p>Average Score: <xsl:value-of select="./avgScore"/> from <xsl:value-of select="./@max-score-instance"/></p>
	
	<h2>File Section</h2>
	<p>Provider: <xsl:value-of select="./file-section/provider"/></p>
	<p>Number of files: <xsl:value-of select="./file-section/numOfFiles"/></p>
	<p>Total size: <xsl:value-of select="./file-section/size"/> B</p>
	<p>Average size: <xsl:value-of select="./file-section/avgSize"/> B</p>
	<p>Minimal file size: <xsl:value-of select="./file-section/minFileSize"/> B</p>
	<p>Maximal file size: <xsl:value-of select="./file-section/maxFileSize"/> B</p>
	
	<h2>Header Section</h2>
	<table border="1" cellpadding="1" cellspacing="1">
		<caption>Profiles in Collection</caption>
		<thead>
			<tr>
				<th>ID</th>
				<th>Count</th>
			</tr>
		</thead>
		<tfoot>
			<tr>
				<td>Total number of profiles: <xsl:value-of select="./header-section/profiles/@count"/></td>
			</tr>
		</tfoot>
		<tbody>
		<xsl:for-each select="./header-section/profiles/profiles">
		<tr>
			<td><xsl:value-of select="./@name"/></td>
			<td><xsl:value-of select="./@count"/></td>
	    </tr>
		</xsl:for-each>
		</tbody>
	</table>
	
	<h2>ResourceProxy Section</h2>
	<p>Total number of resource proxies: <xsl:value-of select="./resProxy-section/totNumOfResProxies"/></p>
	<p>Average number of resource proxies: <xsl:value-of select="./resProxy-section/avgNumOfResProxies"/></p>
	<p>Total number of resource proxies with MIME: <xsl:value-of select="./resProxy-section/totNumOfResProxiesWithMime"/></p>
	<p>Average number of resource proxies with MIME: <xsl:value-of select="./resProxy-section/avgNumOfResProxiesWithMime"/></p>
	<p>Total number of resource proxies with reference: <xsl:value-of select="./resProxy-section/totNumOfResProxiesWithReferences"/></p>
	<p>Average number of resource proxies with references: <xsl:value-of select="./resProxy-section/avgNumOfResProxiesWithReferences"/></p>
	
	
	<h2>XML Validation Section</h2>
	<p>Total number of XML elements: <xsl:value-of select="./xml-validation-section/totNumOfXMLElements"/></p>
	<p>Average number of XML elements: <xsl:value-of select="./xml-validation-section/avgNumOfXMLElements"/></p>
	<p>Total number of simple XML elements: <xsl:value-of select="./xml-validation-section/totNumOfXMLSimpleElements"/></p>
	<p>Average number of simple XML elements: <xsl:value-of select="./xml-validation-section/avgNumOfXMLSimpleElements"/></p>
	<p>Total number of empty XML elements: <xsl:value-of select="./xml-validation-section/totNumOfXMLEmptyElement"/></p>
	<p>Average number of empty XML elements: <xsl:value-of select="./xml-validation-section/avgXMLEmptyElement"/></p>
	<p>Average rate of populated elements: <xsl:value-of select="./xml-validation-section/avgRateOfPopulatedElements"/>%</p>
	
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
	
	<h2>Facet Section</h2>
	<p>Average facet coverage: <xsl:value-of select="./facet-section/avgFacetCoverageByInstance"/>%</p>
	
	<h2>Invalid files in collection</h2>
	<table border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr><th>File</th></tr>
		</thead>
		<tfoot>
			<tr><td>Total: <xsl:value-of select="count(./invalidFilesList/invalidFile)"/></td></tr>
		</tfoot>
		<tbody>
		<xsl:for-each select="./invalidFilesList">
			<tr><td><xsl:value-of select="./invalidFile"/></td></tr>
		</xsl:for-each>
		</tbody>
	</table>

<h3><a href="#!ResultView/xml/">See XML</a></h3>
</body>
</html>
</xsl:template>
</xsl:stylesheet>