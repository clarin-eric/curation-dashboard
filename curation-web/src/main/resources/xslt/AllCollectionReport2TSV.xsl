<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:functx="http://www.functx.com">
	
	<xsl:function name="functx:capitalize-first"
		as="xs:string?" xmlns:functx="http://www.functx.com">
		<xsl:param name="arg" as="xs:string?" />
		<xsl:sequence
			select="concat(upper-case(substring($arg,1,1)),substring($arg,2))" />
	</xsl:function>

   <xsl:output method="text" encoding="UTF-8" indent="no" />
	<xsl:strip-space elements="*" />
	<xsl:template match="allCollectionReport">
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
      <xsl:apply-templates select="collection" mode="value" />
	</xsl:template>
	<xsl:template match="collection" mode="value">
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
         <xsl:value-of select="avgNumOfResources"></xsl:value-of>
         <xsl:text>&#9;</xsl:text>
         <xsl:value-of select="numOfResources"></xsl:value-of>
         <xsl:text>&#9;</xsl:text>
         <xsl:value-of select="ratioOfValidRecords"></xsl:value-of>
         <xsl:text>&#9;</xsl:text>
         <xsl:value-of select="avgNumOfEmptyXMLElements"></xsl:value-of>
         <xsl:text>&#9;</xsl:text>
         <xsl:apply-templates select="facets/facet" mode="value" />	
	</xsl:template>
	<xsl:template match="facets/facet" mode="value">
      <xsl:text>&#9;</xsl:text>
      <xsl:value-of select="@avgCoverage"></xsl:value-of>	
	</xsl:template>
</xsl:stylesheet>
