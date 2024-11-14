<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:functx="http://www.functx.com"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:function name="functx:capitalize-first" as="xs:string?">
		<xsl:param name="arg" as="xs:string?"/>
		<xsl:sequence select="concat(upper-case(substring($arg,1,1)), substring($arg,2))"/>
	</xsl:function>
	<xsl:output method="html" indent="yes"/>
	<xsl:decimal-format NaN="N/A"/>
	<xsl:template match="/allProfileReport">
		<html>
			<body>
				<div class="infoLine">
					<div class="floatLeft">
						created at
						<xsl:value-of select="./@creationTime"/>
					</div>
					<div class="floatRight">download as
						<a href="/download/profile/AllProfileReport">xml</a>
						<xsl:text> </xsl:text>
						<a href="/download/profile/AllProfileReport?format=json">json</a>
						<xsl:text> </xsl:text>
						<a href="/download/profile/AllProfileReport?format=tsv">tsv</a>
					</div>
					<div class="clear"/>
				</div>
				<table id="profiles" class="display text-nowrap" width="100%">
					<thead>
						<tr>
							<th>Id</th>
							<th>Name</th>
							<th>(P)ublic<br />(C)R resident</th>
							<th>Score</th>
							<th>Collection Usage</th>
							<th>Instance Usage</th>
							<th>FacetCoverage</th>
							<xsl:for-each select="./profile[1]/facets/facet">
								<th>
									<xsl:value-of select="functx:capitalize-first(@name)"/>
								</th>
							</xsl:for-each>
							<th>Perc Of Elements With Concepts</th>
							<th>SMC</th>
						</tr>
					</thead>
					<tbody>
						<xsl:for-each select="profile">
							<tr>
								<td>
									<a>
										<xsl:attribute name="href">
											<xsl:text>/profile/</xsl:text>
											<xsl:value-of select="translate(reportName,'.:','__')"/>
											<xsl:text>.html</xsl:text>
										</xsl:attribute>
										<xsl:value-of select="@id"/>
									</a>
								</td>
								<td>
									<xsl:value-of select="name"/>
								</td>
								<td>
									<xsl:if test="@public">P</xsl:if>
									<xsl:if test="@crResident">C</xsl:if>
								</td>
								<td>
									<xsl:value-of select="format-number(score,'0.00')"/>
								</td>
								<td>
									<xsl:value-of select="format-number(collectionUsage, '###,##0')"/>
								</td>
								<td>
									<xsl:value-of select="format-number(instanceUsage, '###,##0')"/>
								</td>
								<td>
									<xsl:value-of select="format-number(facetCoverage,'0.00%')"/>
								</td>

								<xsl:for-each select="./facets/facet">
									<xsl:choose>
										<xsl:when test="@coveredByProfile = 'true'">
											<td class="facetCovered">
												<xsl:value-of select="@coveredByProfile"/>
											</td>
										</xsl:when>
										<xsl:otherwise>
											<td class="facetNotCovered">
												<xsl:value-of select="@coveredByProfile"/>
											</td>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
								<td>
									<xsl:value-of
											select="format-number(percOfElementsWithConcept,'0.00%')"/>
								</td>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>