<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/linkcheckerDetailReport">
		<html>
			<head>
			</head>
			<body>
				<div class="creation-time">
					created at
					<xsl:value-of select="./@creationTime" />
				</div>
				<div class="clear" />
				<h1>Linkchecker Detail Report</h1>
				<h2>
					Collection name:
					<xsl:value-of select="replace(@provider,'_',' ')" />
				</h2>
				<xsl:apply-templates select="categoryReport" />
			</body>
		</html>


	</xsl:template>
	<xsl:template match="categoryReport">
		<h3>
			<xsl:attribute name="id"><xsl:value-of
				select="./@category" /></xsl:attribute>
			Category:
			<xsl:value-of select="./@category" />
		</h3>
		<div>
			Download full category <xsl:value-of select="./@category" /> list as zipped
			<a>
				<xsl:attribute name="href">
	  <xsl:text>/download/linkchecker/</xsl:text>
	  <xsl:value-of
					select="/linkcheckerDetailReport/@provider" />
	  <xsl:text>/</xsl:text>
	  <xsl:value-of select="@category" />
  </xsl:attribute>
				<xsl:text>xml</xsl:text>
			</a>
			<xsl:text> </xsl:text>  
			<a>
            <xsl:attribute name="href">
               <xsl:text>/download/linkchecker/</xsl:text>
               <xsl:value-of select="/linkcheckerDetailReport/@provider" />
               <xsl:text>/</xsl:text>
               <xsl:value-of select="@category" />
               <xsl:text>?format=json</xsl:text>
            </xsl:attribute>
            <xsl:text>json</xsl:text>			
			</a>
			<xsl:text> </xsl:text> 
			<a>
            <xsl:attribute name="href">
               <xsl:text>/download/linkchecker/</xsl:text>
               <xsl:value-of select="/linkcheckerDetailReport/@provider" />
               <xsl:text>/</xsl:text>
               <xsl:value-of select="@category" />
               <xsl:text>?format=tsv</xsl:text>
            </xsl:attribute>
            <xsl:text>tsv</xsl:text>  			
			</a>
		</div>
		<table class="reportTable">
			<thead>
				<tr>
					<th>Url</th>
					<th>Status</th>
					<th>Info</th>
				</tr>
			</thead>

			<xsl:apply-templates select="status" />
		</table>

	</xsl:template>
	<xsl:template match="status">
		<tr>
			<td>
				<xsl:attribute name="class">
	          <xsl:value-of select="../@name" />
	          </xsl:attribute>


				<a>
					<xsl:attribute name="href">
                 <xsl:value-of select="./@url" />
             </xsl:attribute>
					<xsl:value-of select="./@url" />
				</a>
			</td>
			<td>
				<xsl:choose>
					<xsl:when test="./@statusCode  &gt; 0">
						<xsl:value-of select="./@statusCode" />
					</xsl:when>
					<xsl:otherwise>
						N/A
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<td>
				<button class="showUrlInfo btn btn-info">Show</button>
			</td>
		</tr>
		<tr hidden="hidden">
			<td colspan="3">
				<b>Message: </b>
				<xsl:value-of select="./@message" />
				<br />
				<b>Content length: </b>
				<xsl:value-of select="./@contentLength" />
				<br />
				<b>Expected mime-type: </b>
				<xsl:value-of select="./@expectedMimeType" />
				<br />
				<b>Content type: </b>
				<xsl:value-of select="./@contentType" />
				<br />
				<b>Request duration(ms): </b>
				<xsl:value-of select="./@duration" />
				<br />
				<b>Method: </b>
				<xsl:value-of select="./@method" />
				<br />
				<b>Checking date: </b>
				<xsl:value-of select="./@checkingDate" />
				<br />
				<b>Origin: </b>
				<xsl:value-of select="./@origin" />
			</td>
		</tr>

	</xsl:template>
</xsl:stylesheet>