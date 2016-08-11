<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/profile-report">
<html>
<body style="font-family: ‘Lucida Sans Unicode’, ‘Lucida Grande’, sans-serif;">

	<xsl:variable name="schemaLoc"><xsl:value-of select="./header-section/schemaLocation" /></xsl:variable>
	
	<h1>Profile's report for: <xsl:value-of select="./header-section/name"/></h1>
	
	<p>Id: <xsl:value-of select="./header-section/id"/></p>
	<p>Schema location: <a href="{$schemaLoc}" target="_blank"><xsl:copy-of select="$schemaLoc" /></a></p>
	<p>Description: <xsl:value-of select="./header-section/description"/></p>
	<p>CMDI Version: <xsl:value-of select="./header-section/cmdiVersion"/></p>
	<xsl:if test="./header-section/status">
	<p>status: <xsl:value-of select="./header-section/status"/></p>
	</xsl:if>
	
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
	<table border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr>
				<th scope="col">name</th>
				<th scope="col">covered</th>
			</tr>
		</thead>
		<tfoot>
			<tr><td colspan="2"><b>
				total: <xsl:value-of select="./facets-section/@numOfFacets"/>
				covered: <xsl:value-of select="./facets-section/@coveredByProfile"/>
				coverage: <xsl:value-of select="./facets-section/@profileCoverage"/>
			</b></td></tr>
		</tfoot>
		<tbody>
		<xsl:for-each select="./facets-section/facet">
		<tr>	
			<td><xsl:value-of select="./@name"/></td>
			<td><xsl:value-of select="./@covered"/></td>
	    </tr>
		</xsl:for-each>
		</tbody>
	</table>
	<hr/>
	<h2>cmd-component-section</h2>	
	<table border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr>
				<th scope="col">name</th>
				<th scope="col">id</th>
				<th scope="col">count</th>
			</tr>
		</thead>
		<tfoot>
			<tr><td colspan="3"><b>
				total: <xsl:value-of select="./cmd-components-section/@total"/>
				unique: <xsl:value-of select="./cmd-components-section/@unique"/>
				required: <xsl:value-of select="./cmd-components-section/@required"/>
			</b></td></tr>
		</tfoot>
		<tbody>
		<xsl:for-each select="./cmd-components-section/component">
		<tr>
			<xsl:variable name="href">http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/<xsl:value-of select="./@id" /></xsl:variable>
			<td><xsl:value-of select="./@name"/></td>
			<td><a href="{$href}" target="_blank"><xsl:value-of select="./@id" /></a></td>
			<td><xsl:value-of select="./@count"/></td>
	    </tr>
		</xsl:for-each>
		</tbody>
	</table>
	<hr/>
	<h2>cmd-concepts-section</h2>
	<p>total number of elements: <xsl:value-of select="./cmd-concepts-section/@total"/></p>
	<p>number of required elements: <xsl:value-of select="./cmd-concepts-section/@required"/></p>
	<p>number of elements with specified concept: <xsl:value-of select="./cmd-concepts-section/@withConcept"/></p>
	<p>percentage of elements with specified concept: <xsl:value-of select="./cmd-concepts-section/@percWithConcept"/></p>
	<table border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr>
				<th scope="col">url</th>
				<th scope="col">count</th>
			</tr>
		</thead>
		<tfoot>
			<tr><td colspan="2"><b>
				total: <xsl:value-of select="./cmd-concepts-section/concepts/@total"/>
				unique: <xsl:value-of select="./cmd-concepts-section/concepts/@unique"/>
				required: <xsl:value-of select="./cmd-concepts-section/concepts/@required"/>
			</b></td></tr>
		</tfoot>
		<tbody>
		<xsl:for-each select="./cmd-concepts-section/concepts/concept">
		<tr>
			<xsl:variable name="href"><xsl:value-of select="./@ccr" /></xsl:variable>
			<td><a href="{$href}" target="_blank"><xsl:value-of select="./@ccr" /></a></td>
			<td><xsl:value-of select="./@count"/></td>
	    </tr>
		</xsl:for-each>
		</tbody>
	</table>
	
	
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