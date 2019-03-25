<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/collections-report">
	<html>
	<body>
	<table>

	<thead>
	  <tr>
	  <th>Name</th>
	  <th>Score</th>
	  <th>Num Of Records</th>
	  <th>Num Of Profiles</th>
	  <th>Num Of Unique Links</th>
	  <th>No Of Checked Links</th>
	  <th>Ratio Of Valid Links</th>
	  <th>Num Of Res Proxies</th>
	  <th>Avg Num Of Res Proxies</th>
	  <th>Ratio Of Valid Records</th>
	  <th>Avg Num Of Empty XML Elements</th>
	  <th>Avg Facet Coverage</th>

	      <xsl:for-each select="./collection[1]/facets/facet">
	    <th><xsl:value-of select="@name"></xsl:value-of></th>
	</xsl:for-each> 
	  </tr>
	 	</thead>
	
  <tbody>
		<xsl:for-each select="collection">
		<tr>
		<td><xsl:value-of select="@name"></xsl:value-of></td>
    <td><xsl:value-of select="format-number(scorePercentage,'##.##%')"></xsl:value-of></td>
    <td><xsl:value-of select="numOfFiles"></xsl:value-of></td>
    <td><xsl:value-of select="numOfProfiles"></xsl:value-of></td>
    <td><xsl:value-of select="numOfUniqueLinks"></xsl:value-of></td>
    <td><xsl:value-of select="numOfCheckedLinks"></xsl:value-of></td>
    <td><xsl:value-of select="format-number(ratioOfValidLinks,'##.##%')"></xsl:value-of></td>
    <td><xsl:value-of select="numOfResProxies"></xsl:value-of></td>
    <td><xsl:value-of select="format-number(avgNumOfResProxies,'##.#')"></xsl:value-of></td>
    <td><xsl:value-of select="format-number(ratioOfValidRecords,'##.##%')"></xsl:value-of></td>
    <td><xsl:value-of select="format-number(avgNumOfEmptyXMLElements,'##.#')"></xsl:value-of></td>
    <td><xsl:value-of select="format-number(avgFacetCoverage,'##.##%')"></xsl:value-of></td>
    
    <xsl:for-each select="facets/facet">
    <td><xsl:value-of select="format-number(.,'##.##%')"></xsl:value-of></td>
    </xsl:for-each>
		</tr>
		</xsl:for-each>
  </tbody>
	

		
		</table>
		</body>
		</html>
	</xsl:template>	
</xsl:stylesheet>