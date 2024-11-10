<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:functx="http://www.functx.com">
<xsl:function name="functx:capitalize-first" as="xs:string?"
              xmlns:functx="http://www.functx.com">
  <xsl:param name="arg" as="xs:string?"/>

  <xsl:sequence select="
   concat(upper-case(substring($arg,1,1)),
             substring($arg,2))
 "/>

</xsl:function>
<xsl:output method="text" encoding="UTF-8" indent="no"/>
<xsl:strip-space elements="*"/>
	<xsl:template match="/allProfileReport">
			<xsl:text>Id</xsl:text>
<xsl:text>&#9;</xsl:text>
<xsl:text>Name</xsl:text>
<xsl:text>&#9;</xsl:text>
<xsl:text>(P)ublic (C)R resident</xsl:text>
<xsl:text>&#9;</xsl:text>
<xsl:text>Score</xsl:text>
<xsl:text>&#9;</xsl:text>
<xsl:text>Collection Usage</xsl:text>
<xsl:text>&#9;</xsl:text>
<xsl:text>Instance Usage</xsl:text>
<xsl:text>&#9;</xsl:text>
<xsl:text>Facet Coverage</xsl:text>
<xsl:for-each select="./profile[1]/facets/facet">
	<xsl:text>&#9;</xsl:text>
	<xsl:value-of select="functx:capitalize-first(@name)"></xsl:value-of>
</xsl:for-each>
<xsl:text>&#9;</xsl:text>
<xsl:text>Perc Of Elements With Concepts</xsl:text>
<xsl:for-each select="profile">
<xsl:text>&#xa;</xsl:text>
<xsl:value-of select="@id"></xsl:value-of>
<xsl:text>&#9;</xsl:text>
<xsl:value-of select="name"></xsl:value-of>
<xsl:text>&#9;</xsl:text>
<xsl:if test="@public">P</xsl:if><xsl:if test="@crResident">C</xsl:if>
<xsl:text>&#9;</xsl:text>
<xsl:value-of select="score"></xsl:value-of>
<xsl:text>&#9;</xsl:text>
<xsl:value-of select="collectionUsage"></xsl:value-of>
<xsl:text>&#9;</xsl:text>
<xsl:value-of select="instanceUsage"></xsl:value-of>
<xsl:text>&#9;</xsl:text>
<xsl:value-of select="facetCoverage"></xsl:value-of>
<xsl:for-each select="facets/facet">
    <xsl:text>&#9;</xsl:text>
    <xsl:value-of select="@coveredByProfile"></xsl:value-of>
</xsl:for-each>
<xsl:text>&#9;</xsl:text>
<xsl:value-of select="percOfElementsWithConcept"></xsl:value-of>
</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>