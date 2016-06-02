<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/profile-report">
<html>
<body style="font-family: ‘Lucida Sans Unicode’, ‘Lucida Grande’, sans-serif;">
	<h1>Profile's report for: <xsl:value-of select="./header-section/name"/></h1>
	
	<p>Id: <xsl:value-of select="./header-section/ID"/></p>
	<p>Description: <xsl:value-of select="./header-section/description"/></p>
	<p>isPublic: <xsl:value-of select="./header-section/isPublic"/></p>
	<p>Score: <xsl:value-of select="./score"/> from <xsl:value-of select="./@max-score"/></p>
	
	<h2>cmd-component-section</h2>
	<p>total number of components in profile: <xsl:value-of select="./cmd-components-section/total"/></p>
	<p>number of unique components: <xsl:value-of select="./cmd-components-section/unique"/></p>
	<p>number of required components: <xsl:value-of select="./cmd-components-section/required"/></p>
	
	<h2>facets-section</h2>
	<!--
	<p>total number of facets (from facetConcept.xml): <xsl:value-of select="facets-section/numOfFacets"/></p>
	-->
	<p>number of covered facets: <xsl:value-of select="./facets-section/profile/numOfCoveredFacets"/>/<xsl:value-of select="./facets-section/numOfFacets"/></p>
	<p>coverage: <xsl:value-of select="./facets-section/profile/coverage"/>%</p>
	<p>facets not being covered: 
	<xsl:for-each select="./facets-section/profile/not-covered/facet">
	  <xsl:value-of select="."/>
	  <xsl:if test="position() &lt; last()">, </xsl:if>
	</xsl:for-each>
	</p>
	
	<h2>cmd-concepts-section</h2>
	<p>total number of elements: <xsl:value-of select="./cmd-concepts-section/total"/></p>
	<p>number of unique elements: <xsl:value-of select="./cmd-concepts-section/unique"/></p>
	<p>number of required elements: <xsl:value-of select="./cmd-concepts-section/required"/></p>
	<p>number of elements with specified concept: <xsl:value-of select="./cmd-concepts-section/withConcept"/></p>
	<p>percentage of elements with specified concept: <xsl:value-of select="./cmd-concepts-section/percWithConcept"/>%</p>
	<p>number of required elements with specified concept: <xsl:value-of select="./cmd-concepts-section/concepts/@required"/></p>
	<table border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr>
				<th scope="col">Concept URL</th>
				<th scope="col">Count</th>
			</tr>
		</thead>
		<tfoot>
			<tr><td>
				total: <xsl:value-of select="./cmd-concepts-section/concepts/@total"/>
				unique: <xsl:value-of select="./cmd-concepts-section/concepts/@unique"/>
				required: <xsl:value-of select="./cmd-concepts-section/concepts/@required"/>
			</td></tr>
		</tfoot>
		<tbody>
		<xsl:for-each select="./cmd-concepts-section/concepts/concept">
		<tr>
			<xsl:variable name="href"><xsl:value-of select="./@url" /></xsl:variable>
			<td><a href="{$href}"><xsl:value-of select="./@url" /></a></td>
			<td><xsl:value-of select="./@count"/></td>
	    </tr>
		</xsl:for-each>
		</tbody>
	</table>
	
	
	<h2>Issues occurred during the assessment</h2>
	<table border="1" cellpadding="1" cellspacing="1">
		<thead>
			<tr>
				<th scope="col">Section</th>
				<th scope="col">Severity</th>
				<th scope="col">Message</th>
			</tr>
		</thead>
		<tbody>
		<xsl:for-each select="./details/messages">
		<tr>
			<td>Profile</td>
			<td><xsl:value-of select="./@lvl"/></td>
			<td><xsl:value-of select="./@message"/></td>
	    </tr>
		</xsl:for-each>
		</tbody>
	</table>

</body>
</html>
</xsl:template>
</xsl:stylesheet> 