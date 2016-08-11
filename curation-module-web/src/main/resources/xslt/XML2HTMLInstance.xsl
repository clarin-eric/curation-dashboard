<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/instance-report">
<html>
<body style="font-family: ‘Lucida Sans Unicode’, ‘Lucida Grande’, sans-serif;">
	<h1 align="center">CMD Record Report</h1>
	
	<xsl:variable name="profileID" select="./profile-section/id" />
	<xsl:variable name="cmdRecord" select="./file-section/location" />
	
	<p>CMD Record:  <a href="{$cmdRecord}" target="_blank"><xsl:copy-of select="$cmdRecord" /></a></p>
	<p>profileID: <a href="#!ResultView/profile/{$profileID}"><xsl:copy-of select="$profileID"/></a></p>
	<p>file size: <xsl:value-of select="./file-section/size"/> B</p> 
	<hr/>
	
	<h2>score-section</h2>
	<table border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr>
				<th scope="col">segment</th>
				<th scope="col">score</th>
				<th scope="col">max</th>
			</tr>
		</thead>
		<tfoot>
			<tr><td colspan="3"><b>
				instance: <xsl:value-of select="./@ins-score"/>
				total: <xsl:value-of select="./@score"/>
				max: <xsl:value-of select="./@max-score"/>
			</b></td></tr>
		</tfoot>
		<tbody>
		<xsl:for-each select="./score-section/score">
		<tr>
			<td><xsl:value-of select="./@segment"/></td>
			<td><xsl:value-of select="./@score"/></td>
			<td><xsl:value-of select="./@maxScore"/></td>
	    </tr>
		</xsl:for-each>
		</tbody>
	</table>
	
		<hr/>
	
	<h2>facets-section</h2>	
	<table style="width:100%" border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr>
				<th scope="col">Facet</th>
				<th scope="col">Value</th>
				<th scope="col">Concept</th>
				<th scope="col">XPath</th>
			</tr>
		</thead>
		<tfoot>
			<tr><td colspan="4"><b>
				total: <xsl:value-of select="./facets-section/@numOfFacets"/> 				
				coveredByInstance: <xsl:value-of select="./facets-section/@coveredByInstance"/> 
				instanceCoverage: <xsl:value-of select="./facets-section/@instanceCoverage"/> 
				coveredByProfile: <xsl:value-of select="./facets-section/@coveredByProfile"/> 
				profileCoverage: <xsl:value-of select="./facets-section/@profileCoverage"/>
			</b></td></tr>
		</tfoot>
		<tbody>
		
		<xsl:for-each select="./facets-section/facet">		
			<xsl:choose>
			  <xsl:when test="./entry">
				<xsl:for-each select="./entry">
					<tr>								
						<xsl:if test="position() = 1">
							<th rowspan="{last()}"><xsl:value-of select="../@name"/></th>
						</xsl:if>				
						<th><xsl:value-of select="@value"/></th>
						<td><a href="{@concept}" target="_blank"><xsl:value-of select="@concept"/></a></td>
						<td><xsl:value-of select="@xpath"/></td>
					</tr>
				</xsl:for-each>
			  </xsl:when>
			  <xsl:otherwise>
				<tr>
					<th><xsl:value-of select="./@name"/></th>
					<th colspan="3">missing value</th>
				</tr>
			  </xsl:otherwise>
			</xsl:choose>			
		</xsl:for-each>
		</tbody>
	</table>
	
	
	<hr/>	
	
	<h2>resProxy-section</h2>
	<p>total number ResourceProxies: <xsl:value-of select="./resProxy-section/numOfResProxies"/></p>
	<p>number of ResourceProxies having specified MIME type: <xsl:value-of select="./resProxy-section/numOfResourcesWithMime"/></p>
	<p>percent of ResourceProxies having specified MIME type: <xsl:value-of select="./resProxy-section/percOfResourcesWithMime"/></p>
	<p>number of ResourceProxies having reference: <xsl:value-of select="./resProxy-section/numOfResProxiesWithReferences"/></p>
	<p>percent of ResourceProxies having reference: <xsl:value-of select="./resProxy-section/percOfResProxiesWithReferences"/></p>	
	<table border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr>
				<th scope="col">resource type</th>
				<th scope="col">count</th>
			</tr>
		</thead>
		<tbody>
		<xsl:for-each select="./resProxy-section/resourceTypes/resourceType">
		<tr>
			<td><xsl:value-of select="./@type"/></td>
			<td><xsl:value-of select="./@count"/></td>
	    </tr>
		</xsl:for-each>
		</tbody>
	</table>
	
	<hr/>	
	
	<h2>xml-validation-section</h2>
	<p>number of XML elements: <xsl:value-of select="./xml-validation-section/numOfXMLElements"/></p>
	<p>number of simple XML elements: <xsl:value-of select="./xml-validation-section/numOfXMLSimpleElements"/></p>
	<p>number of empty XML elements: <xsl:value-of select="./xml-validation-section/numOfXMLEmptyElement"/></p>
	<p>percentage of populated XML elements: <xsl:value-of select="./xml-validation-section/percOfPopulatedElements"/></p>
	
	<hr/>	
	
	<h2>url-validation-section</h2>
	<p>number of links: <xsl:value-of select="./url-validation-section/numOfLinks"/></p>
	<p>number of unique links: <xsl:value-of select="./url-validation-section/numOfUniqueLinks"/></p>
	<p>number of links in resourceProxy reference <xsl:value-of select="./url-validation-section/numOfResProxiesLinks"/></p>
	<p>number of broken links <xsl:value-of select="./url-validation-section/numOfBrokenLinks"/></p>
	<p>percentage of valid links XML elements: <xsl:value-of select="./url-validation-section/percOfValidLinks"/></p>
	

	<xsl:if test="./score-section//issue">
	
		<hr/>	
		<h2>Issues</h2>
		<table border="1" cellpadding="1" cellspacing="1">
			<thead>
				<tr>
					<th scope="col">segment</th>
					<th scope="col">severity</th>
					<th scope="col">message</th>
				</tr>
			</thead>
			<tbody>
			<xsl:for-each select="./score-section/score">
			<xsl:variable name="seg"><xsl:value-of select="./@segment" /></xsl:variable>
				<xsl:for-each select="./issue">
			<tr>
				<td><xsl:copy-of select="$seg" /></td>
				<td><xsl:value-of select="./@lvl"/></td>
				<td><xsl:value-of select="./@message"/></td>
		    </tr>
		    	</xsl:for-each>
		    	
			</xsl:for-each>
			</tbody>
		</table>
	</xsl:if>
	
</body>
</html>
</xsl:template>
</xsl:stylesheet> 