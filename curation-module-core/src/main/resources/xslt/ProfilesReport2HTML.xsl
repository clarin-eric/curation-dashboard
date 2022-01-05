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
	<xsl:output method="html" indent="yes"></xsl:output>
	<xsl:template match="/profiles">
		<html>
			<body>
				<div class="creation-time">
					created at
					<xsl:value-of select="./@creation-time" />
				</div>
				<div class="download">download as <a href="/download/xml/profiles/ProfilesReport">xml</a><xsl:text> </xsl:text><a href="/download/json/profiles/ProfilesReport">json</a><xsl:text> </xsl:text><a href="/download/tsv/profiles/ProfilesReport">tsv</a></div>
				<div class="clear" />
				<table id="profiles" class="display text-nowrap" width="100%">
					<thead>
						<tr>
							<th>Id</th>
							<th>Name</th>
							<th>Score</th>
							<th>Collection Usage</th>
							<th>Instance Usage</th>
							<th>FacetCoverage</th>
							<xsl:for-each select="./profile[1]/facets/facet">
								<th>
									<xsl:value-of
										select="functx:capitalize-first(@name)"></xsl:value-of>
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
                                    <xsl:value-of
											select="translate(reportName,'.:','__')"></xsl:value-of>
                                    <xsl:text>.html</xsl:text>
                                </xsl:attribute>
										<xsl:value-of select="@id"></xsl:value-of>
									</a>
								</td>
								<td>
									<xsl:value-of select="name"></xsl:value-of>
								</td>
								<td>
									<xsl:value-of select="format-number(score,'0.00')"></xsl:value-of>
								</td>
								<td>
									<xsl:value-of
										select="format-number(collectionUsage, '0')"></xsl:value-of>
								</td>
								<td>
									<xsl:value-of
										select="format-number(instanceUsage, '0')"></xsl:value-of>
								</td>
								<td>
									<xsl:value-of
										select="format-number(facetCoverage,'0.00%')"></xsl:value-of>
								</td>

								<xsl:for-each select="facets/facet">
									<xsl:choose>
										<xsl:when test="@covered = 'true'">
											<td class="facetCovered">
												<xsl:value-of select="@covered"></xsl:value-of>
											</td>
										</xsl:when>
										<xsl:otherwise>
											<td class="facetNotCovered">
												<xsl:value-of select="@covered"></xsl:value-of>
											</td>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
								<td>
									<xsl:value-of
										select="format-number(percOfElementsWithConcept,'0.00%')"></xsl:value-of>
								</td>
								<td>
									<a>
										<xsl:attribute name="href">
                                    <xsl:text>https://clarin.oeaw.ac.at/exist/apps/smc-browser/index.html?graph=smc-graph-groups-profiles-datcats-rr.js&amp;depth-before=7&amp;depth-after=2&amp;link-distance=120&amp;charge=250&amp;friction=75&amp;gravity=10&amp;node-size=4&amp;link-width=1&amp;labels=show&amp;curve=straight-arrow&amp;layout=horizontal-tree&amp;selected=</xsl:text>
                                    <xsl:value-of
											select="translate(@id,'.:','_')"></xsl:value-of>
                                </xsl:attribute>
										explore in SMC browser
									</a>
								</td>
							</tr>
						</xsl:for-each>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>