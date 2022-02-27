<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:functx="http://www.functx.com">
	<xsl:function name="functx:capitalize-first"
		as="xs:string?" xmlns:functx="http://www.functx.com">
		<xsl:param name="arg" as="xs:string?" />

		<xsl:sequence
			select="
   concat(upper-case(substring($arg,1,1)),
             substring($arg,2))
 " />

	</xsl:function>
	<xsl:output method="text" encoding="UTF-8" indent="no" />
	<xsl:strip-space elements="*" />
	<xsl:template match="/collections-report">
		<xsl:text>Name</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Score</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Num Of Records</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Num Of Profiles</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Num Of Unique Links</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>No Of Checked Links</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Ratio Of Valid Links</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Avg Num Of Res Proxies</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Num Of Res Proxies</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Ratio Of Valid Records</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Avg Num Of Empty XML Elements</xsl:text>
		<xsl:text>&#9;</xsl:text>
		<xsl:text>Ratio Facets Non Zero</xsl:text>
		<xsl:for-each select="./collection[1]/facets/facet">
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="functx:capitalize-first(@name)"></xsl:value-of>
		</xsl:for-each>
		<xsl:for-each select="collection">
			<xsl:text>&#xa;</xsl:text>
			<xsl:value-of select="@name">
			</xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="scorePercentage"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="numOfFiles"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="numOfProfiles"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="numOfUniqueLinks"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="numOfCheckedLinks"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="ratioOfValidLinks"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="avgNumOfResProxies"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="numOfResProxies"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="ratioOfValidRecords"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="avgNumOfEmptyXMLElements"></xsl:value-of>
			<xsl:text>&#9;</xsl:text>
			<xsl:value-of select="avgFacetCoverage"></xsl:value-of>
			<xsl:for-each select="facets/facet">
				<xsl:text>&#9;</xsl:text>
				<xsl:value-of select="."></xsl:value-of>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
