<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/instance-report">
<html>
	<head>
		<link rel="stylesheet" type="text/css" href="./VAADIN/themes/mytheme/xsltStyle.css?v=7.6.4" />
	</head>
	<body>
		<h1>CMD Record Report</h1>

		<xsl:variable name="profileID" select="./profile-section/id" />
		<xsl:variable name="cmdRecord" select="./file-section/location" />

		<p>CMD Record:
			<xsl:choose>
				<xsl:when  test="starts-with($cmdRecord, 'http')">
					<a href="{$cmdRecord}" target="_blank"><xsl:copy-of select="$cmdRecord" /></a>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="$cmdRecord" />
				</xsl:otherwise>
			</xsl:choose>
		</p>
		<p>profileID: <a href="#!ResultView/profile/id/{$profileID}"><xsl:copy-of select="$profileID"/></a></p>
		<p>file size: <xsl:value-of select="./file-section/size"/> B</p>
		<hr/>

		<h2>score-section</h2>
		<table>
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
					scorePercentage: <xsl:value-of select="./@score-percentage"/>
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

		<font color="#dbd839">&#9873;</font> - derived facet

		<br/>


		<button id="toggle" type="button" onClick="toggle()" >Show Facet Values</button>

		<table>
			<thead>
				<tr>
					<th scope="col">Value</th>
					<th scope="col">Facet</th>
					<th scope="col">Normalised Value</th>
					<th scope="col">Concept</th>
					<th scope="col">XPath</th>
				</tr>
			</thead>
			<tfoot>
				<tr><td colspan="5"><b>
					covered by Instance
					<xsl:value-of select="count(./facets-section/coverage/facet[@coveredByInstance = 'true'])" /> /
					<xsl:value-of select="./facets-section/@numOfFacets"/>;

					covered by profile
					<xsl:value-of select="count(./facets-section/coverage/facet[@coveredByProfile = 'true'])" /> /
					<xsl:value-of select="./facets-section/@numOfFacets"/>;

					instance coverage: <xsl:value-of select="./facets-section/@instanceCoverage"/>;
					profile coverage: <xsl:value-of select="./facets-section/@profileCoverage"/>
				</b></td></tr>
			</tfoot>
			<tbody>

			<xsl:for-each select="./facets-section/values/valueNode">
				<xsl:choose>
				  <xsl:when test="./facet">
					<xsl:for-each select="./facet">
						<tr>
							<xsl:if test="position() = 1">
								<th rowspan="{last()}">
									<xsl:value-of select="../value"/>
								</th>
							</xsl:if>
							<xsl:choose>
				  				<xsl:when test="@isDerived">
				  					<th><font color="#dbd839"><xsl:value-of select="@name"/></font></th>
				  				</xsl:when>
				  				<xsl:otherwise>
				  					<th><xsl:value-of select="@name"/></th>
				  				</xsl:otherwise>
				  			</xsl:choose>
							<td><xsl:value-of select="@normalisedValue"/></td>
							<xsl:if test="position() = 1">
								<td  rowspan="{last()}">
									<a href="{../concept/@uri}" title="status: {../concept/@status}, uri: {../concept/@uri}" target="_blank"><xsl:value-of select="../concept/@prefLabel"/></a>
								</td>
							</xsl:if>
							<xsl:if test="position() = 1">
								<td rowspan="{last()}">
									<xsl:value-of select="../xpath"/>
								</td>
							</xsl:if>
						</tr>
					</xsl:for-each>
				  </xsl:when>
				  <xsl:otherwise>
					<tr class="noFacet">
						<th><xsl:value-of select="value"/></th>
						<td></td>
						<td></td>
						<td><a href="{concept/@uri}" title="status: {concept/@status}, uri: {concept/@uri}" target="_blank"><xsl:value-of select="concept/@prefLabel"/></a></td>
						<td><xsl:value-of select="xpath"/></td>
					</tr>
				  </xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>

			<xsl:for-each select="./facets-section/coverage/facet[@coveredByProfile = 'false']">
					<tr>
						<th><font color="#d33d3d">not covered by profile</font></th>
						<th><font color="#d33d3d"><xsl:value-of select="@name"/></font></th>
						<td></td>
						<td></td>
						<td></td>
					</tr>
			</xsl:for-each>

			<xsl:for-each select="./facets-section/coverage/facet[@coveredByInstance = 'false'][@coveredByProfile = 'true']">
					<tr>
						<th><font color="#d33d3d">not covered by instance</font></th>
						<th><font color="#d33d3d"><xsl:value-of select="@name"/></font></th>
						<td></td>
						<td></td>
						<td></td>
					</tr>
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
		<table>
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
		<p>validity according to profile: <xsl:value-of select="./xml-validation-section/valid"/></p>

		<hr/>

		<h2>xml-populated-section</h2>
		<p>number of XML elements: <xsl:value-of select="./xml-populated-section/numOfXMLElements"/></p>
		<p>number of simple XML elements: <xsl:value-of select="./xml-populated-section/numOfXMLSimpleElements"/></p>
		<p>number of empty XML elements: <xsl:value-of select="./xml-populated-section/numOfXMLEmptyElement"/></p>
		<p>percentage of populated XML elements: <xsl:value-of select="./xml-populated-section/percOfPopulatedElements"/></p>

		<hr/>

		<h2>url-validation-section</h2>
		<p>number of links: <xsl:value-of select="./url-validation-section/numOfLinks"/></p>
		<p>number of unique links: <xsl:value-of select="./url-validation-section/numOfUniqueLinks"/></p>
		<p>number of broken links <xsl:value-of select="./url-validation-section/numOfBrokenLinks"/></p>
		<p>percentage of valid links XML elements: <xsl:value-of select="./url-validation-section/percOfValidLinks"/></p>

		<hr/>
		<h2>single-url-report</h2>
		<table border="1" cellpadding="1" cellspacing="1">
			<thead>
				<tr>
					<th scope="col">url</th>
					<th scope="col">message</th>
					<th scope="col">http-status</th>
					<th scope="col">content-type</th>
					<th scope="col">byte-size</th>
					<th scope="col">request-duration</th>
					<th scope="col">timestamp</th>
				</tr>
			</thead>
			<tbody>
				<xsl:for-each select="./single-url-report/url">
					<tr>
                        <td><xsl:copy-of select="."/></td>
                        <td><xsl:value-of select="./@message" /></td>
                        <td><xsl:value-of select="./@http-status" /></td>
                        <td><xsl:value-of select="./@content-type" /></td>
                        <td><xsl:value-of select="./@byte-size" /></td>
                        <td><xsl:value-of select="./@request-duration" /></td>
                        <td><xsl:value-of select="./@timestamp" /></td>
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
						<xsl:choose>
			  				<xsl:when test="@lvl = 'ERROR'">
			  				  	<tr>
									<td><font color="#d33d3d"><xsl:copy-of select="$seg" /></font></td>
									<td><font color="#d33d3d"><xsl:value-of select="./@lvl"/></font></td>
									<td><font color="#d33d3d"><xsl:value-of select="./@message"/></font></td>
							   	</tr>
							</xsl:when>
							<xsl:when test="@lvl = 'WARNING'">
								<tr>
									<td><font color="#dbd839"><xsl:copy-of select="$seg" /></font></td>
									<td><font color="#dbd839"><xsl:value-of select="./@lvl"/></font></td>
									<td><font color="#dbd839"><xsl:value-of select="./@message"/></font></td>
							  	</tr>
							</xsl:when>
			  				<xsl:otherwise>
								<tr>
									<td><xsl:copy-of select="$seg" /></td>
									<td><xsl:value-of select="./@lvl"/></td>
									<td><xsl:value-of select="./@message"/></td>
							    </tr>
			  				</xsl:otherwise>
			  			</xsl:choose>
			    	</xsl:for-each>
				</xsl:for-each>
				</tbody>
			</table>
		</xsl:if>

	</body>
</html>
</xsl:template>
</xsl:stylesheet> 