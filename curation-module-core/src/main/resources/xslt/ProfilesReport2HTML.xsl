<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/profiles">
	<table>
	<thead>
<tr>
<td>Id</td>
<td>Name</td>
<td>Score</td>
<td>Facet Coverage</td>
<xsl:for-each select="./profile[1]/facets/facet">
	    <th><xsl:value-of select="@name"></xsl:value-of></th>
	</xsl:for-each>
	<td>Perc Of Elements With Concepts</td>
	<td>SMC</td>
</tr>
	</thead>
	<tbody>
		<xsl:for-each select="profile">
		<tr>
		<td><xsl:value-of select="@id"></xsl:value-of> </td>
		<td><xsl:value-of select="name"></xsl:value-of></td>
		<td><xsl:value-of select="score"></xsl:value-of></td>
		<td><xsl:value-of select="format-number(facetCoverage,'##.##%')"></xsl:value-of></td>
		
		   <xsl:for-each select="facets/facet">
		   <xsl:choose>
    <xsl:when test="@covered = 'true'">
      <td class="facetCovered"><xsl:value-of select="@covered"></xsl:value-of></td>
    </xsl:when>
    <xsl:otherwise>
    <td class="facetNotCovered"><xsl:value-of select="@covered"></xsl:value-of></td>
    </xsl:otherwise>
		   </xsl:choose>
    </xsl:for-each>
    <td><xsl:value-of select="format-number(percOfElementsWithConcept,'##.##%')"></xsl:value-of></td>
    <td>
    <a>
    <xsl:attribute name="href">
      <xsl:text>https%3A%2F%2Fclarin.oeaw.ac.at%2Fexist%2Fapps%2Fsmc-browser%2Findex.html%3Fgraph%3Dsmc-graph-groups-profiles-datcats-rr.js%26depth-before%3D7%26depth-after%3D2%26link-distance%3D120%26charge%3D250%26friction%3D75%26gravity%3D10%26node-size%3D4%26link-width%3D1%26labels%3Dshow%26curve%3Dstraight-arrow%26layout%3Dhorizontal-tree%26selected%3D</xsl:text>
      <xsl:value-of select="translate(@id,':','_')"></xsl:value-of>
    </xsl:attribute>explore in SMC browser
    </a>
    </td>
   </tr>
		</xsl:for-each>
	</tbody>	
		</table>
	</xsl:template>
</xsl:stylesheet>