<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/instance-report">
<html>
<body style="font-family: ‘Lucida Sans Unicode’, ‘Lucida Grande’, sans-serif;">
	<h1>Report for the Instance: <xsl:value-of select="file-section/location"/></h1>
	
	<p>profile's score: <xsl:value-of select="./score-profile"/></p>
	<p>instance's score: <xsl:value-of select="./score-instance"/></p>
	<p>total score: <xsl:value-of select="./score-total"/> from <xsl:value-of select="./@max-score"/></p>
	<p>profile: <xsl:value-of select="./header-section/profile"/></p>
	
	<h2>file-section</h2>
	<p>location: <xsl:value-of select="./file-section/location"/></p>
	<p>file size: <xsl:value-of select="./file-section/size"/> B</p> 
	
	<h2>resProxy-section</h2>
	<p>total number ResourceProxies: <xsl:value-of select="resProxy-section/numOfResProxies"/></p>
	<p>number of ResourceProxies having specified MIME type: <xsl:value-of select="resProxy-section/numOfResProxiesWithMime"/></p>
	<p>percent of ResourceProxies having specified MIME type: <xsl:value-of select="resProxy-section/percOfResProxiesWithMime"/>%</p>
	<p>number of ResourceProxies having reference: <xsl:value-of select="resProxy-section/numOfResProxiesWithReferences"/></p>
	<p>percent of ResourceProxies having reference: <xsl:value-of select="resProxy-section/percOfResProxiesWithReferences"/>%</p>
	<p>types of resources: 
		<xsl:for-each select="//resourceType">
		  <xsl:value-of select="./@type"/>(<xsl:value-of select="./@count"/>)
		  <xsl:if test="position() &lt; last()">, </xsl:if>
		</xsl:for-each>
	</p>
	
	<h2>xml-validation-section</h2>
	<p>number of XML elements: <xsl:value-of select="xml-validation-section/numOfXMLElements"/></p>
	<p>number of simple XML elements: <xsl:value-of select="xml-validation-section/numOfXMLSimpleElements"/></p>
	<p>number of empty XML elements: <xsl:value-of select="xml-validation-section/numOfXMLEmptyElement"/></p>
	<p>percentage of populated XML elements: <xsl:value-of select="xml-validation-section/percOfPopulatedElements"/>%</p>
	
	<h2>url-validation-section</h2>
	<p>number of links: <xsl:value-of select="url-validation-section/numOfLinks"/></p>
	<p>number of unique links: <xsl:value-of select="url-validation-section/numOfUniqueLinks"/></p>
	<p>number of links in resourceProxy reference <xsl:value-of select="url-validation-section/numOfResProxiesLinks"/></p>
	<p>number of broken inks <xsl:value-of select="url-validation-section/numOfBrokenLinks"/></p>
	<p>percentage of valid links XML elements: <xsl:value-of select="url-validation-section/percOfValidLinks"/>%</p>
	
	<h2>facets-section</h2>
	<p>number of covered facets: <xsl:value-of select="//numOfCoveredFacets"/>/<xsl:value-of select="//numOfFacets"/></p>
	<p>coverage: <xsl:value-of select="facets-section/profile/coverage"/>%</p>
	<p>facets not being covered: 
		<xsl:for-each select="facets-section/profile/not-covered/facet">
		  <xsl:value-of select="."/>
		  <xsl:if test="position() &lt; last()">, </xsl:if>
		</xsl:for-each>
	</p>
	<p>facets with missing value: 
		<xsl:for-each select="facets-section/instance/missingValues/facet">
		  <xsl:value-of select="."/>
		  <xsl:if test="position() &lt; last()">, </xsl:if>
		</xsl:for-each>
	</p>
	
	<p>Facet values</p>
	<ul>
		<xsl:for-each select="facets-section/instance/facet">
		<li><xsl:value-of select="./@name"/></li>
		<ul>
			<xsl:for-each select="./value">
			<li><xsl:value-of select="."/></li>
			</xsl:for-each>
		</ul>
		</xsl:for-each>
	</ul>
	
	<h2>Issues occured during the assesment</h2>
	<table border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr>
				<th scope="col">Section</th>
				<th scope="col">Severity</th>
				<th scope="col">Message</th>
			</tr>
		</thead>
		<tbody>
		<xsl:for-each select="details">
		<tr>
			<td>General</td>
			<td><xsl:value-of select="./@lvl"/></td>
			<td><xsl:value-of select="./@message"/></td>
	    </tr>
		</xsl:for-each>
		<xsl:for-each select="header-section/details/messages">
		<tr>
			<td>Header</td>
			<td><xsl:value-of select="./@lvl"/></td>
			<td><xsl:value-of select="./@message"/></td>
	    </tr>
		</xsl:for-each>
		<xsl:for-each select="file-section/details/messages">
		<tr>
			<td>File</td>
			<td><xsl:value-of select="./@lvl"/></td>
			<td><xsl:value-of select="./@message"/></td>
	    </tr>
		</xsl:for-each>
		
		<xsl:for-each select="resProxy-section/details/messages">
		<tr>
			<td>ResourceProxy</td>
			<td><xsl:value-of select="./@lvl"/></td>
			<td><xsl:value-of select="./@message"/></td>
	    </tr>
		</xsl:for-each>
		
		<xsl:for-each select="xml-validation-section/details/messages">
		<tr>
			<td>XML validation</td>
			<td><xsl:value-of select="./@lvl"/></td>
			<td><xsl:value-of select="./@message"/></td>
	    </tr>
		</xsl:for-each>
		
		<xsl:for-each select="url-validation-section/details/messages">
		<tr>
			<td>URL validation</td>
			<td><xsl:value-of select="./@lvl"/></td>
			<td><xsl:value-of select="./@message"/></td>
	    </tr>
		</xsl:for-each>
		
		<xsl:for-each select="facets-section/details/messages">
		<tr>
			<td>Facets</td>
			<td><xsl:value-of select="./@lvl"/></td>
			<td><xsl:value-of select="./@message"/></td>
	    </tr>
		</xsl:for-each>
		</tbody>
	</table>
	
	<h3><a href="#!ResultView/xml/">See XML</a></h3>

</body>
</html>
</xsl:template>
</xsl:stylesheet> 