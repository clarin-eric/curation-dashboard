<?xml version="2.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"></xsl:output>
<html>
<head></head>
<body>

	<xsl:template match="/">
	
	<h2>Collections:</h2>
	
		<xsl:for-each select="collection">
		<h3><xsl:value-of select="@name"></xsl:value-of></h3>
		<table>
		<thead>
		<tr>
		<th>Status</th>
		<th>Category</th>
		<th>Count</th>
		<th>Average Response Duration(ms)</th>
		<th>Max Response Duration(ms)</th>
		</tr>
		</thead>
		<tbody>
		<xsl:for-each select="statistics">
		<tr>
		<td><xsl:value-of select="statusCode"></xsl:value-of></td>
		<td><xsl:value-of select="category"></xsl:value-of></td>
		<td><xsl:value-of select="count"></xsl:value-of></td>
		<td><xsl:value-of select="avgRespTime"></xsl:value-of></td>
		<td><xsl:value-of select="maxRespTime"></xsl:value-of></td>
		</tr>
		
		</xsl:for-each>
		
		</tbody>
		
		</table>
		
		</xsl:for-each>
	</xsl:template>
</body>
</html>
</xsl:stylesheet>
